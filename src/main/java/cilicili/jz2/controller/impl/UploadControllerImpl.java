package cilicili.jz2.controller.impl;

import cilicili.jz2.controller.IUploadController;
import cilicili.jz2.pojo.Token;
import cilicili.jz2.utils.BaseUtil;
import cilicili.jz2.utils.ConvertVideoTest;
import cilicili.jz2.utils.RandomUtil;
import cilicili.jz2.utils.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UploadControllerImpl implements IUploadController {
	@RequestMapping (value = "/upload/{token}", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public Map<String, Serializable> upload(MultipartFile file, @PathVariable ("token") String token) {
		Map<String, Serializable> result = new HashMap<>();
		do {
			result.put("status", "failure");
			try {
				TokenUtil.checkToken(token, TokenUtil.TokenUssage.UPLOAD_FILE);
			} catch (TokenUtil.TokenExpired | TokenUtil.TokenNotFound | TokenUtil.TokenOverAuthed | TokenUtil.TokenUssageNotMatched tokenError) {
				result.put("msg", tokenError.getMessage());
				break;
			}
			if (file == null || file.isEmpty()) {
				result.put("msg", "没有选择文件");
			} else {
				long fileSize = file.getSize();
				if (fileSize >= BaseUtil.MAX_FILE_SIZE) {
					result.put("msg", "文件体积超过上限");
					break;
				}
				String fileOriginalName = file.getOriginalFilename();
				String[] fileOriginalNameArr = fileOriginalName.split("\\.");
				String filename = fileOriginalName.substring(0, fileOriginalName.lastIndexOf("."));
				String extension = fileOriginalNameArr[fileOriginalNameArr.length - 1];
				String storageFilename;
				File storageFile;
				do {
					storageFilename = RandomUtil.getRandomFilename(extension, filename, token);
					storageFile = new File(BaseUtil.STORAGE_DIR + storageFilename);
				} while (storageFile.exists());
				try {
					file.transferTo(storageFile);
					result.put("status", "success");
					result.put("msg", "上传成功");
					result.put("url", storageFilename);
				} catch (IOException e) {
					Logger logger = LoggerFactory.getLogger(this.getClass());
					logger.error(e.getMessage());
					logger.error(e.getLocalizedMessage());
					result.put("msg", "上传失败");
				}
			}
		} while (false);
		return result;
	}

	@RequestMapping (value = "/test/upload/{token}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Serializable> uploadtest(MultipartFile file, @PathVariable ("token") String token) {
		Map<String, Serializable> result = new HashMap<>();
		do {
			result.put("status", "failure");
			try {
				TokenUtil.checkToken(token, TokenUtil.TokenUssage.UPLOAD_FILE);
			} catch (TokenUtil.TokenExpired | TokenUtil.TokenNotFound | TokenUtil.TokenOverAuthed | TokenUtil.TokenUssageNotMatched tokenError) {
				result.put("msg", tokenError.getMessage());
				break;
			}
			if (file == null || file.isEmpty()) {
				result.put("msg", "没有选择文件");
			} else {
				long fileSize = file.getSize();
				if (fileSize >= BaseUtil.MAX_FILE_SIZE) {
					result.put("msg", "文件体积超过上限");
					break;
				}
				String path = "E:/Projectpicture/websiteimages/temp/";

				File TempFile = new File(path);
				if (TempFile.exists()) {
					if (TempFile.isDirectory()) {
						System.out.println("该文件夹存在。");
					}else {
						System.out.println("同名的文件存在，不能创建文件夹。");
					}
				}else {
					System.out.println("文件夹不存在，创建该文件夹。");
					TempFile.mkdir();
				}

				// 获取上传时候的文件名
				String filename = file.getOriginalFilename();

				// 获取文件后缀名
				String filename_extension = filename.substring(filename
						.lastIndexOf(".") + 1);
				//System.out.println("视频的后缀名:"+filename_extension);

				//时间戳做新的文件名，避免中文乱码-重新生成filename
				long filename1 = new Date().getTime();
				filename = Long.toString(filename1)+"."+filename_extension;

				//去掉后缀的文件名
				String filename2 = filename.substring(0, filename.lastIndexOf("."));
				//System.out.println("视频名为:"+filename2);

				//源视频地址+重命名后的视频名+视频后缀
				String yuanPATH =(path+filename);

				//System.out.println("视频的完整文件名1:"+filename);
				//System.out.println("源视频路径为:"+yuanPATH);

				//上传到本地磁盘/服务器
				try {
					//System.out.println("写入本地磁盘/服务器");
					InputStream is = file.getInputStream();
					OutputStream os = new FileOutputStream(new File(path, filename));
					int len = 0;
					byte[] buffer = new byte[2048];

					while ((len = is.read(buffer)) != -1) {
						os.write(buffer, 0, len);
					}
					os.close();
					os.flush();
					is.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					result.put("msg", "服务器出了点故障");
					return result;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					result.put("msg", "上传失败");
					return result;
				}
				try {
					//System.out.println("========上传完成，开始调用转码工具类=======");
					ConvertVideoTest c = new ConvertVideoTest();
					c.run(yuanPATH);   //调用转码
					//System.out.println("=================转码过程彻底结束=====================");
					//获取转码后的mp4文件名
					/*String Mp4path = "E://Projectpicture/websiteimages/finishvideo/";
					filename2 = filename2+".mp4";
					String NewVideopath =Mp4path +filename2;
					System.out.println("新视频的url:"+NewVideopath);*/
					result.put("status", "success");
					result.put("msg", "上传成功");
					result.put("url", filename2+".mp4");
					result.put("picUrl",filename2+".jpg");
					//删除临时文件
					File file2 = new File(path);
					/*if (!file2.exists()) {
						System.out.println("没有该文件");
					}
					if (!file2.isDirectory()) {
						System.out.println("没有该文件夹");
					}*/
					String[] tempList = file2.list();
					File temp = null;
					for (int i = 0; i < tempList.length; i++) {
						if (path.endsWith(File.separator)) {
							temp = new File(path + tempList[i]);
						} else {
							temp = new File(path + File.separator + tempList[i]);
						}
						if (temp.isFile() || temp.isDirectory()) {
							temp.delete();		//删除文件夹里面的文件
						}
					}
					//System.out.println("所有的临时视频文件删除成功");
				}catch (Exception e){
					Logger logger = LoggerFactory.getLogger(this.getClass());
					logger.error(e.getMessage());
					logger.error(e.getLocalizedMessage());
					result.put("msg", "上传失败");
				}

				/*String fileOriginalName = file.getOriginalFilename();
				String[] fileOriginalNameArr = fileOriginalName.split("\\.");
				String filename = fileOriginalName.substring(0, fileOriginalName.lastIndexOf("."));
				String extension = fileOriginalNameArr[fileOriginalNameArr.length - 1];
				String storageFilename;
				File storageFile;
				do {
					storageFilename = RandomUtil.getRandomFilename(extension, filename, token);
					storageFile = new File(BaseUtil.STORAGE_DIR + storageFilename);
				} while (storageFile.exists());
				try {
					file.transferTo(storageFile);
					result.put("status", "success");
					result.put("msg", "上传成功");
					result.put("url", storageFilename);
				} catch (IOException e) {
					Logger logger = LoggerFactory.getLogger(this.getClass());
					logger.error(e.getMessage());
					logger.error(e.getLocalizedMessage());
					result.put("msg", "上传失败");
				}*/
			}
		} while (false);
		return result;
	}

	@RequestMapping (value = "/404", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Serializable> handleError() throws Exception {
		throw new Exception();
	}

	@ResponseBody
	@ExceptionHandler ({Exception.class})
	public Map<String, Serializable> exceptionHandle(Exception e) {
		Map<String, Serializable> result = new HashMap<>();
		result.put("status", "failure");
		result.put("msg", "未登录或参数错误");
		Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.error(e.getMessage());
		logger.error(e.getLocalizedMessage());
		return result;
	}
}
