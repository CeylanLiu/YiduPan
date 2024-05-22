package com.easypan.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.CreateImageCode;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.VerifyRegexEnum;
import com.easypan.entity.po.EmailCode;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.exception.BusinessException;
import com.easypan.service.EmailCodeService;
import com.easypan.service.UserInfoService;
import com.easypan.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户信息 Controller
 */
@RestController("userInfoController")
//@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController{
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_VALUE = "application/json;charset-UTF-8";
	private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class);


	@Resource
	private RedisComponent redisComponent;

	@Resource
	private UserInfoService userInfoService;

	@Resource
	private EmailCodeService emailCodeService;

	@Resource
	private AppConfig appConfig;

	@RequestMapping("/checkCode")
	public void checkCode(HttpServletResponse response, HttpSession session
			, @RequestParam(value = "type", required = false) Integer type) throws IOException {
		CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);

		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache"); //响应消息不能缓存
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");

		String code = vCode.getCode();
		if (type == null || type == 0) {
			session.setAttribute(Constants.CHECK_CODE_KEY, code);
		} else {
			session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
		}
		vCode.write(response.getOutputStream());
	}

	@RequestMapping("/sendEmailCode")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO sendEmailCode(HttpSession session, String email,String checkCode,Integer type) {
	try {
		if(!checkCode.equalsIgnoreCase((String)session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL) )){
			throw new BusinessException("验证码错误");
		}
		// 如果验证码正确，发送邮箱验证码
		emailCodeService.sendEmailCode(email, type);
		return getSuccessResponseVO(null);
	} catch (MessagingException e) {
        throw new RuntimeException(e);
    } finally {
		// 删除session中保存的邮箱验证码
		session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
	}
	}

	@RequestMapping("/register")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO register(HttpSession session, String nickName,String email,String password,String checkCode,String emailCode) {
		try {
			if(!checkCode.equalsIgnoreCase((String)session.getAttribute(Constants.CHECK_CODE_KEY) )){
				throw new BusinessException("验证码错误");
			}
			userInfoService.register(email, nickName, password, emailCode);
			return getSuccessResponseVO(null);
		} finally {
			// 删除session中保存的邮箱验证码
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}

	@PostMapping("/login")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public ResponseVO login(HttpSession session,
							@VerifyParam(required = true) String email,
							@VerifyParam(required = true) String password,
							@VerifyParam(required = true) String checkCode) {
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				throw new BusinessException("图片验证码不正确");
			}
			SessionWebUserDto sessionWebUserDto = userInfoService.login(email, password);
			session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
			return getSuccessResponseVO(null);
		} finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}

	@RequestMapping("/resetPwd")
	@GlobalInterceptor(checkParams = true , checkLogin = false)
	public ResponseVO resetPwd(HttpSession session, String email, String password, String checkCode, String emailCode) {
		try {
			if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
				throw new BusinessException("图片验证码不正确");
			}
			userInfoService.resetPwd(email, password, emailCode);
			return getSuccessResponseVO(null);
		} finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}


	// 获取用户头像
	@GetMapping("/getAvatar/{userId}")
	@GlobalInterceptor(checkParams = true, checkLogin = false)
	public void getAvatar(HttpServletResponse response,
						  @VerifyParam(required = true) @PathVariable("userId") String userId) {
		// 得到头像根目录 = /file + /avatar
		String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
		// 从AppConfig中得到项目根目录，从而得到放置头像文件夹的绝对路径
		File folder = new File(appConfig.getProjectFolder() + avatarFolderName);
		if (!folder.exists()) {
			// 不存在就创建
			folder.mkdirs();
		}

		// 根据userId得到用户头像的绝对路径 = 放置头像文件夹的绝对路径 + userId + .jpg(统一后缀)

		String avatarPath = appConfig.getProjectFolder() + avatarFolderName + "/" + userId + Constants.AVATAR_SUFFIX;
		File file = new File(avatarPath);
		// 如果找不到该用户的头像
		if (!file.exists()) {
			// 默认头
			avatarPath = appConfig.getProjectFolder() + avatarFolderName + "/" + Constants.AVATAR_DEFAULT;
			// 获取系统默认头像
			if (!new File(avatarPath).exists()) {
				// 获取默认头像失败
				printNoDefaultImage(response);
				return;
			}

		}
		// 输出
		response.setContentType("image/jpg");
		readFile(response, avatarPath);

	}

	// 解决输出默认头像失败问题
	private void printNoDefaultImage(HttpServletResponse response) {
		response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
		response.setStatus(HttpStatus.OK.value());
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.print("请在头像目录下放置默认头像default_avatar.jpg");
			writer.close();
		} catch (Exception e) {
			logger.error("输出无默认图失败", e);
		} finally {
			writer.close();
		}
	}

	@GetMapping("/getUserInfo")
	@GlobalInterceptor
	public ResponseVO getUserInfo(HttpSession session) {
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		return getSuccessResponseVO(sessionWebUserDto);
	}

	@PostMapping("/getUseSpace")
	@GlobalInterceptor
	public ResponseVO getUseSpace(HttpSession session) {
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		return getSuccessResponseVO(redisComponent.getUserSpaceUse(sessionWebUserDto.getUserId()));
	}

	@RequestMapping("/logout")
	public ResponseVO logout(HttpSession session) {
		session.invalidate();
		return getSuccessResponseVO(null);
	}


	@PostMapping("/updateUserAvatar")
	@GlobalInterceptor
	public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar) {

		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		// 得到头像文件夹
		String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
		File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
		// 如果不存在就创建
		if (!targetFileFolder.exists()) {
			targetFileFolder.mkdirs();
		}
		// 得到新头像绝对路径
		File targetFile = new File(targetFileFolder.getPath() + "/" + webUserDto.getUserId() + Constants.AVATAR_SUFFIX);
		try {
			// 输出
			avatar.transferTo(targetFile);
		} catch (Exception e) {
			logger.error("上传头像失败", e);
		}

		// 同时将数据库中qq头像设为空
		UserInfo userInfo = new UserInfo();
		userInfo.setQqAvatar("");
		userInfoService.updateUserInfoByUserId(userInfo, webUserDto.getUserId());
		webUserDto.setAvatar(null);
		//更新session
		session.setAttribute(Constants.SESSION_KEY, webUserDto);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/updatePassword")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO updatePassword(HttpSession session,
									 @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min=0,max=10) String password) {
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		UserInfo userInfo = new UserInfo();
		userInfo.setPassword(StringTools.encodeByMD5(password));
		userInfoService.updateUserInfoByUserId(userInfo,sessionWebUserDto.getUserId());

		return getSuccessResponseVO(null);
	}
}