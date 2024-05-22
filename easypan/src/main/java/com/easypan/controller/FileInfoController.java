package com.easypan.controller;

import java.util.List;

import com.easypan.annotation.GlobalInterceptor;
import com.easypan.annotation.VerifyParam;
import com.easypan.entity.constants.Constants;
import com.easypan.entity.dto.SessionWebUserDto;
import com.easypan.entity.dto.UploadResultDto;
import com.easypan.entity.enums.FileCategoryEnums;
import com.easypan.entity.enums.FileDelFlagEnums;
import com.easypan.entity.query.FileInfoQuery;
import com.easypan.entity.po.FileInfo;
import com.easypan.entity.vo.FileInfoVO;
import com.easypan.entity.vo.PaginationResultVO;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.service.FileInfoService;
import com.easypan.utils.CopyTools;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 文件信息表 Controller
 */
@RestController("fileInfoController")
@RequestMapping("/file")
public class FileInfoController extends CommonFileController{

	@Resource
	private FileInfoService fileInfoService;
	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("/loadDataList")
	@GlobalInterceptor
	public ResponseVO loadDataList(HttpSession session,FileInfoQuery query,String category){
		FileCategoryEnums categoryEnum = FileCategoryEnums.getByCode(category);
		if (categoryEnum != null) {
			query.setFileCategory(categoryEnum.getCategory());
		}
		query.setUserId(getUserInfoFromSession(session).getUserId());
		// 排序规则：先展示文件夹，然后按照修改时间排序
		query.setOrderBy("last_update_time desc");
		query.setDelFlag(FileDelFlagEnums.USING.getFlag());
		// 调用service方法得到分页对象，其中List<FileInfo>
		PaginationResultVO result = fileInfoService.findListByPage(query);
		// 集合元素映射为FileInfoVO再返回
//		convert2PaginationVO(result, FileInfoVO.class);
//		ResponseVO tmp = getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
//		return tmp;
		return getSuccessResponseVO(convert2PaginationVO(result,FileInfoVO.class));
	}



	public SessionWebUserDto getUserInfoFromSession(HttpSession session) {
		SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
		return sessionWebUserDto;
	}


	@PostMapping("/uploadFile")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO uploadFile(HttpSession session,
								 String fileId,
								 MultipartFile file,
								 @VerifyParam(required = true) String fileName,
								 @VerifyParam(required = true) String filePid,
								 @VerifyParam(required = true) String fileMd5,
								 @VerifyParam(required = true) Integer chunkIndex,
								 @VerifyParam(required = true) Integer chunks) {

		// 从session中获取SessionWebUserDto对象
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);

		UploadResultDto resultDto = fileInfoService.uploadFile(webUserDto, fileId, file, fileName,
				filePid, fileMd5, chunkIndex, chunks);

		return getSuccessResponseVO(resultDto);
	}

	// 得到缩略图
	@GetMapping("/getImage/{imageFolder}/{imageName}")
	public void getImage(HttpServletResponse response,
						 @PathVariable("imageFolder") String imageFolder,
						 @PathVariable("imageName") String imageName) {
		super.getImage(response, imageFolder, imageName);
	}


	//处理视频文件
	@GetMapping("/ts/getVideoInfo/{fileId}")
	public void getVideoInfo(HttpServletResponse response,
							 HttpSession session,
							 @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		super.getFile(response, fileId, webUserDto.getUserId());
	}


	//处理其他文件
	@RequestMapping("/getFile/{fileId}")
	public void getFile(HttpServletResponse response,
						HttpSession session,
						@PathVariable("fileId") @VerifyParam(required = true) String fileId) {
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		super.getFile(response, fileId, webUserDto.getUserId());
	}


	@PostMapping("/newFoloder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO newFolder(HttpSession session,
								@VerifyParam(required = true) String filePid,
								@VerifyParam(required = true) String fileName) {
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		FileInfo fileInfo = fileInfoService.newFolder(filePid, webUserDto.getUserId(), fileName);
		return getSuccessResponseVO(fileInfo);
	}


	@PostMapping("/createDownloadUrl/{fileId}")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO createDownloadUrl(HttpSession session,
										@PathVariable("fileId") @VerifyParam(required = true) String fileId) {
		return super.createDownloadUrl(fileId, getUserInfoFromSession(session).getUserId());
	}
	// 无需登陆校验
	@GetMapping("/download/{code}")
	@GlobalInterceptor(checkLogin = false, checkParams = true)
	public void download(HttpServletRequest request,
						 HttpServletResponse response,
						 @PathVariable("code") @VerifyParam(required = true) String code) throws Exception {
		super.download(request, response, code);
	}


	@PostMapping("/delFile")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO delFile(HttpSession session,
							  @VerifyParam(required = true) String fileIds) {
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		fileInfoService.removeFile2RecycleBatch(webUserDto.getUserId(), fileIds);
		return getSuccessResponseVO(null);
	}

	@PostMapping("/getFolderInfo")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO getFolderInfo(HttpSession session,
									@VerifyParam(required = true) String path) {
		return super.getFolderInfo(path, getUserInfoFromSession(session).getUserId());
	}

	@PostMapping("/rename")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO rename(HttpSession session,
							 @VerifyParam(required = true) String fileId,
							 @VerifyParam(required = true) String fileName) {
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		FileInfo fileInfo = fileInfoService.rename(fileId, webUserDto.getUserId(), fileName);
		return getSuccessResponseVO(CopyTools.copy(fileInfo, FileInfoVO.class));
	}

	// 加载除自己外的文件夹
	@PostMapping("/loadAllFolder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO loadAllFolder(HttpSession session,
									@VerifyParam(required = true) String filePid,
									String currentFileIds) {

		List<FileInfo> fileInfoList = fileInfoService.loadAllFolder(
				getUserInfoFromSession(session).getUserId(), filePid, currentFileIds);

		return getSuccessResponseVO(CopyTools.copyList(fileInfoList, FileInfoVO.class));
	}

	@PostMapping("/changeFileFolder")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO changeFileFolder(HttpSession session,
									   @VerifyParam(required = true) String fileIds,
									   @VerifyParam(required = true) String filePid) {
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);
		fileInfoService.changeFileFolder(fileIds, filePid, webUserDto.getUserId());
		return getSuccessResponseVO(null);
	}
}