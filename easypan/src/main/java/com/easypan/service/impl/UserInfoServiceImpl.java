package com.easypan.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.easypan.SysSettingsDto;
import com.easypan.component.RedisComponent;
import com.easypan.entity.config.AppConfig;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UserSpaceDto;
import com.easypan.entity.enums.UserStatusEnum;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.exception.BusinessException;
import com.easypan.mappers.FileInfoMapper;
import com.easypan.service.EmailCodeService;
import com.easypan.service.FileInfoService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import com.easypan.entity.enums.PageSize;
import com.easypan.entity.query.UserInfoQuery;
import com.easypan.entity.po.UserInfo;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.query.SimplePage;
import com.easypan.mappers.UserInfoMapper;
import com.easypan.service.UserInfoService;
import com.easypan.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

	@Resource
	private FileInfoMapper<FileInfo, FileInfoQuery> fileInfoMapper;
	@Resource
	private AppConfig appConfig;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private EmailCodeService emailCodeService;

	@Resource
	private FileInfoService fileInfoService;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserInfo bean) {
		return this.userInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return this.userInfoMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return this.userInfoMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return this.userInfoMapper.deleteByUserId(userId);
	}

	/**
	 * 根据Email获取对象
	 */
	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return this.userInfoMapper.selectByEmail(email);
	}

	/**
	 * 根据Email修改
	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return this.userInfoMapper.updateByEmail(bean, email);
	}

	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return this.userInfoMapper.deleteByEmail(email);
	}

	/**
	 * 根据QqOpenId获取对象
	 */
	@Override
	public UserInfo getUserInfoByQqOpenId(String qqOpenId) {
		return this.userInfoMapper.selectByQqOpenId(qqOpenId);
	}

	/**
	 * 根据QqOpenId修改
	 */
	@Override
	public Integer updateUserInfoByQqOpenId(UserInfo bean, String qqOpenId) {
		return this.userInfoMapper.updateByQqOpenId(bean, qqOpenId);
	}

	/**
	 * 根据QqOpenId删除
	 */
	@Override
	public Integer deleteUserInfoByQqOpenId(String qqOpenId) {
		return this.userInfoMapper.deleteByQqOpenId(qqOpenId);
	}

	/**
	 * 根据NickName获取对象
	 */
	@Override
	public UserInfo getUserInfoByNickName(String nickName) {
		return this.userInfoMapper.selectByNickName(nickName);
	}

	/**
	 * 根据NickName修改
	 */
	@Override
	public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
		return this.userInfoMapper.updateByNickName(bean, nickName);
	}

	/**
	 * 根据NickName删除
	 */
	@Override
	public Integer deleteUserInfoByNickName(String nickName) {
		return this.userInfoMapper.deleteByNickName(nickName);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void register(String email, String nickName, String password, String emailCode) {
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if (null != userInfo) {
			throw new BusinessException("邮箱账号已经存在");
		}
		UserInfo nickNameUser = this.userInfoMapper.selectByNickName(nickName);
		if (null != nickNameUser) {
			throw new BusinessException("昵称已经存在");
		}
		//校验邮箱验证码
		emailCodeService.checkCode(email, emailCode);
		// 获取随机id
		String userId = StringTools.getRandomNumber(15);
		// 根据输入的信息封装UserInfo对象
		userInfo = new UserInfo();

		userInfo.setUserId(userId);
		userInfo.setNickName(nickName);
		userInfo.setEmail(email);
		userInfo.setPassword(StringTools.encodeByMD5(password));
		userInfo.setJoinTime(new Date());

		userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
		// 从redis中取出系统设置
		SysSettingsDto sysSettingsDto = redisComponent.getSysSettingDto();
		// 用户空间
		userInfo.setTotalSpace(sysSettingsDto.getUserInitUseSpace() * Constants.MB);
//		userInfo.setUseSpace(fileInfoMapper.selectUseSpace(userId));
		this.userInfoMapper.insert(userInfo);
	}

	@Override
	public SessionWebUserDto login(String email, String password) {
		UserInfo userInfo = userInfoMapper.selectByEmail(email);
		if (userInfo == null || !userInfo.getPassword().equals(password)) {
			throw new BusinessException("账号或者密码错误");
		}
		// 如果账户被禁用
		if (UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
			throw new BusinessException("账号已禁用");
		}

		UserInfo updateInfo = new UserInfo();
		updateInfo.setLastJoinTime(new Date());
		// 根据id更新用户最后一次操作时间
		String userId = userInfo.getUserId();
		this.userInfoMapper.updateByUserId(updateInfo, userId);

		SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
		sessionWebUserDto.setNickName(userInfo.getNickName());
		sessionWebUserDto.setUserId(userId);
		if (ArrayUtils.contains(appConfig.getAdminEmails().split(","), email)) {
			sessionWebUserDto.setAdmin(true);
		} else {
			sessionWebUserDto.setAdmin(false);
		}
		// 用户空间
		UserSpaceDto userSpaceDto = new UserSpaceDto();
		Long useSpace = fileInfoMapper.selectUseSpace(userInfo.getUserId());
		userSpaceDto.setUseSpace(useSpace);
//		userSpaceDto.setUseSpace(fileInfoMapper.selectUseSpace(userId));
		userSpaceDto.setTotalSpace(userInfo.getTotalSpace());
		redisComponent.saveUserSpaceUse(userInfo.getUserId(), userSpaceDto);
		return sessionWebUserDto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void resetPwd(String email, String password, String emailCode) {
		UserInfo userInfo = userInfoMapper.selectByEmail(email);
		if (userInfo == null) {
			throw new BusinessException("邮箱账号不存在");
		}
		emailCodeService.checkCode(email,emailCode);
		UserInfo updateInfo = new UserInfo();
		updateInfo.setPassword(StringTools.encodeByMD5(password));
		this.userInfoMapper.updateByEmail(updateInfo,email);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUserStatus(String userId, Integer status) {
		UserInfo userInfo = new UserInfo();
		userInfo.setStatus(status);
		String email = userInfoMapper.selectByUserId(userId).getEmail();

		if (ArrayUtils.contains(appConfig.getAdminEmails().split(","), email)) {
			throw new BusinessException("不可更改管理员状态");
		}
		if (UserStatusEnum.DISABLE.getStatus().equals(status)) {
			userInfo.setUseSpace(0L);
			fileInfoService.deleteFileByUserId(userId);
		}
		userInfoMapper.updateByUserId(userInfo, userId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void changeUserSpace(String userId, Integer changeSpace) {
		Long space = changeSpace * Constants.MB;
		this.userInfoMapper.updateUserSpace(userId, null, space);
		redisComponent.resetUserSpaceUse(userId);
	}
}