package com.evmtv.epg.utils;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.springframework.util.StringUtils;

import sun.misc.BASE64Decoder;


import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;
/**
 * 通用工具类
 * 
 * @author fangzhu(fangzhu@evmtv.com)
 * 
 * @time 2013-3-11 下午9:02:41
 */
public class FileUtils {

	/**
	 * 将字符串转为base64
	 * @param s
	 * @return
	 */
	public static String getBASE64(String s) {
		if (s == null)
			return null;
		return (new sun.misc.BASE64Encoder()).encode(s.getBytes());
	}

	/**
	 * 将base64编码的字符串转为对应的字符串
	 * @param s
	 * @return
	 */
	public static String getFromBASE64(String s) {
		if (s == null)
			return null;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			byte[] b = decoder.decodeBuffer(s);
			return new String(b);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取路径下所有文件名
	 * 
	 * @param path
	 * @return
	 */
	public static String[] getFileNames(String path) {
		File file = new File(path);
		String[] name = file.list();
		return name;
	}

	/**
	 * 获取路径下所有文件夹名
	 * 
	 * @param path
	 * @return
	 */
	public static String[] getDirNames(String path) {
		File file = new File(path);
		File[] files = file.listFiles();
		List<String> dirs = new ArrayList<String>();
		for (File dir : files) {
			if (dir.isDirectory() && !dir.isHidden()) {
				dirs.add(dir.getName());
			}
		}
		return dirs.toArray(new String[0]);
	}

	/**
	 * 复制文件目录
	 * 
	 * @param sourceDirPath
	 *            源文件目录路径
	 * @param targetDirPath
	 *            目标文件目录路径
	 * @return 是否复制成功
	 * @throws IOException
	 * 
	 */
	public static boolean copyDir(String sourceDirPath, String targetDirPath)
			throws IOException {
		boolean bool = false;
		targetDirPath = checkFilePathEndSep(targetDirPath);
		// 创建目标文件夹
		dirExists(targetDirPath);
		// 删除该文件夹下所有文件
		CopyFolder.del(targetDirPath);
		// 获取源文件夹当前下的文件或目录
		File sourceFileDir = new File(sourceDirPath);
		if (sourceFileDir.exists()) {
			bool = true;
			File[] files = sourceFileDir.listFiles();
			for (File file : files) {
				String fileName = file.getName(); 
				if (file.isFile()) {
					CopyFolder.copyFile(file, new File(targetDirPath + fileName));
				}
				if (file.isDirectory()) {
					// 复制目录
					String sourceDir = checkFilePathEndSep(sourceDirPath) + fileName;
					String targetDir = checkFilePathEndSep(targetDirPath) + fileName;
					CopyFolder.copyDirectiory(sourceDir, targetDir);
				}
			}
		}
		return bool;
	}

	/**
	 * 检查文件夹末尾是否有“/”，没有添加
	 * 
	 * @param destDir
	 * @return
	 */
	public static String checkFilePathEndSep(String destDir) {
		// 保证文件夹路径最后是"/"或者"\"
		char lastChar = destDir.charAt(destDir.length() - 1);
		if (lastChar != '/' && lastChar != '\\') {
			destDir += "/";
		}
		return destDir;
	}

	/**
	 * 检查文件夹是否存在,不存在创建文件夹
	 * 
	 * @param filePath
	 */
	public static void dirExists(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}
	/**
	 * 读取文件内容
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(String path, String encoding)
			throws IOException {
		InputStreamReader read = null;
		if(encoding != null)
			read = new InputStreamReader(new FileInputStream(new File(path)), encoding);// 考虑到编码格式
		else
			read = new InputStreamReader(new FileInputStream(new File(path)));// 考虑到编码格式
		
		BufferedReader bufferedReader = new BufferedReader(read);
		StringBuilder sb = new StringBuilder();
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			sb.append(line);
		}
		read.close();
		return sb.toString();
	}
	/**
	 * 读取文件中内容
	 * 
	 * @param path
	 *            路径
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(String path) throws IOException {
		String resultStr = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
			byte[] inBuf = new byte[2000];
			int len = inBuf.length;
			int off = 0;
			int ret = 0;
			while ((ret = fis.read(inBuf, off, len)) > 0) {
				off += ret;
				len -= ret;
			}
			resultStr = new String(new String(inBuf, 0, off, "GBK").getBytes());
		} finally {
			if (fis != null)
				fis.close();
		}
		return resultStr;
	}

	/**
	 * 文件转成字节数组
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static byte[] readFileToBytes(String path) throws IOException {
		byte[] b = null;
		InputStream is = null;
		File f = new File(path);
		try {
			is = new FileInputStream(f);
			b = new byte[(int) f.length()];
			is.read(b);
		} finally {
			if (is != null)
				is.close();
		}
		return b;
	}

	/**
	 * 将byte写入文件中
	 * 
	 * @param fileByte
	 * @param filePath
	 * @throws IOException
	 */
	public static void byteToFile(byte[] fileByte, String filePath)
			throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(filePath));
			os.write(fileByte);
			os.flush();
		} finally {
			if (os != null)
				os.close();
		}
	}

	/**
	 * 将目录文件打包成zip
	 * 
	 * @param srcPathName
	 * @param zipFilePath
	 * @return 成功打包true 失败false
	 */
	public static boolean compress(String srcPathName, String zipFilePath) {
		if (StringUtils.hasText(srcPathName) || StringUtils.hasText(zipFilePath))
			return false;

		File zipFile = new File(zipFilePath);
		File srcdir = new File(srcPathName);
		if (!srcdir.exists())
			return false;
		Project prj = new Project();
		Zip zip = new Zip();
		zip.setProject(prj);
		zip.setDestFile(zipFile);
		FileSet fileSet = new FileSet();
		fileSet.setProject(prj);
		fileSet.setDir(srcdir);
		zip.addFileset(fileSet);
		zip.execute();
		return zipFile.exists();
	}

	/**
	 * 折分数组
	 * 
	 * @param ary
	 * @param subSize
	 * @return
	 */
	public static List<List<Object>> splitAry(Object[] ary, int subSize) {
		int count = ary.length % subSize == 0 ? ary.length / subSize : ary.length / subSize + 1;

		List<List<Object>> subAryList = new ArrayList<List<Object>>();

		for (int i = 0; i < count; i++) {
			int index = i * subSize;

			List<Object> list = new ArrayList<Object>();
			int j = 0;
			while (j < subSize && index < ary.length) {
				list.add(ary[index++]);
				j++;
			}

			subAryList.add(list);
		}

		return subAryList;
	}

	/**
	 * @param mobile
	 * @return
	 */
	public static String ArrayToString(Object[] mobile) {
		String destId = "";
		for (Object phone : mobile) {
			destId += " " + (String) phone;
		}
		return destId.trim();
	}

	
	/**
	 * 写入文件
	 * @param inputStream
	 * @param savePath
	 * @return
	 */
	public static boolean writeToFile(InputStream inputStream,String savePath){
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"gbk"));
			File file = new File(savePath);
			if(file.exists()){
				file.delete();
			}
			FileWriter fw = new FileWriter(file,true);
			BufferedWriter bw=new BufferedWriter(fw);
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				bw.append(inputLine);
				bw.newLine();
			}
			bw.flush();
			bw.close();
			br.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	/**
	 * 写入文件
	 * @param content 文件内容
	 * @param savePath
	 * @return
	 */
	public static boolean writeToFile(String content,String savePath){
		String[] contents = content.split("<br/>");
		try {
			File file = new File(savePath);
			if(file.exists()){
				file.delete();
			}
			BufferedWriter bw=new BufferedWriter(new FileWriter(file,true));
			for(String str : contents){
				bw.append(str);
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 * 写入文件
	 * @param content 文件内容
	 * @param savePath
	 * @return
	 */
	public static boolean writeToLog(String content,String savePath){
		try {
			File file = new File(savePath);
			BufferedWriter bw=new BufferedWriter(new FileWriter(file,true));
			bw.append(content);
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 * 解压缩
	 * @param sourceFile 源文件
	 * @param realPath 服务器绝对路径
	 * @param saveFilePath 文件保存相对路径
	 * @param user 当前用户
	 * @return
	 * @throws Exception
	 */
	public static void deCompress(String sourceFile, String realPath,String saveFilePath)
			throws Exception {
		// 保证文件夹路径最后是"/"或者"\"
		if(StringUtils.hasText(realPath))
			realPath = FileUtils.checkFilePathEndSep(realPath);
		saveFilePath = FileUtils.checkFilePathEndSep(saveFilePath);
		// 根据类型，进行相应的解压缩
		String type = getSuffix(sourceFile);
		if (type.equalsIgnoreCase("zip")) {
			unZip(sourceFile, realPath, saveFilePath);
		} else if (type.equalsIgnoreCase("rar")) {
			unrar(sourceFile, realPath,saveFilePath);
		} else {
			throw new Exception("只支持zip和rar格式的压缩包！");
		}
		/**
		 * 解压成功后删除源文件
		 */
		File deleteFile = new File(sourceFile);
		if (deleteFile.exists()) {
			deleteFile.delete();
		}
	}

	/**
	 * 解压zip包
	 * @param unZipfileName 需要解压的zip文件路径名
	 * @param realPath 项目真实路径
	 * @param saveFilePath 文件保存路径，相对服务器项目
	 * @param user 当前用户
	 * @return
	 */
	private static void unZip(String unZipfileName,String realPath,String saveFilePath) {
		byte[] buf = new byte[1024];
		int readedBytes;
		try {
			ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(unZipfileName)));
			ZipEntry zipEntry;
			while ((zipEntry = zipIn.getNextEntry()) != null) {
				if (!zipEntry.isDirectory()) {
					//原始文件名
					String compressFileName = zipEntry.getName().trim();
					String savePath = StringUtils.hasText(realPath)?realPath:"" + saveFilePath;
					FileUtils.dirExists(savePath);
					//文件路径
					String destFile = savePath + compressFileName;
					// 3解压缩文件
					File file = new File(destFile);
					File parent = file.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					if (file.exists()) {
						file.createNewFile();
					}
					//解压缩文件
					FileOutputStream fileOut = new FileOutputStream(file);
					while ((readedBytes = zipIn.read(buf)) > 0) {
						fileOut.write(buf, 0, readedBytes);
					}
					fileOut.close();
				}
			}
			zipIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解压zip格式压缩包 对应的是ant.jar
	 */
	public void unzip(String sourceZip, String destDir) {
		try {
			Project p = new Project();
			Expand e = new Expand();

			e.setProject(p);
			e.setSrc(new File(sourceZip));
			e.setOverwrite(false);
			e.setDest(new File(destDir));
			
			/** ant下的zip工具默认压缩编码为UTF-8编码， 而winRAR软件压缩是用的windows默认的GBK 或者GB2312编码
			 * 所以解压缩时要制定编码格式
			 */
			e.setEncoding("gbk");
			e.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解压rar格式压缩包。
	 * 对应的是java-unrar-0.3.jar，但是java-unrar-0.3.jar又会用到commons-logging-1.1.1.jar
	 * @param sourceRar rar源文件
	 * @param realPath 项目服务器路径
	 * @param saveFilePath 上传文件保存路径,相对于项目下
	 * @return
	 * @throws Exception
	 */
	private static void unrar(String sourceRar,String realPath,String saveFilePath){
		FileOutputStream fos = null;
		try {
			Archive a = new Archive(new File(sourceRar));
			FileHeader fh = a.nextFileHeader();
			while (fh != null) {
				if (!fh.isDirectory()) {
					//原始文件名
					String compressFileName = fh.getFileNameString().trim();
					String savePath =  StringUtils.hasText(realPath)?realPath:"" + saveFilePath;
					FileUtils.dirExists(savePath);
					//文件路径
					String destFile = savePath + compressFileName;
					//输出文件
					File file = new File(destFile);
					// 3解压缩文件
					fos = new FileOutputStream(file);
					a.extractFile(fh, fos);
					fos.close();
					fos = null;
				}
				fh = a.nextFileHeader();
			}
			a.close();
			a = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
					fos = null;
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 获取文件后缀名
	 * 
	 * @param fileName
	 * @return 如：.jpg、.gif
	 */
	public static String getFileSuffix(String fileName) {
		int lastIndexOfDot = fileName.lastIndexOf('.');
		int fileNameLength = fileName.length();
		return fileName.substring(lastIndexOfDot, fileNameLength);
	}
	/**
	 * 获取文件后缀名不要“.”
	 * 
	 * @param fileName
	 * @return 如jpg、gif
	 */
	public static String getSuffix(String fileName) {
		int lastIndexOfDot = fileName.lastIndexOf('.');
		int fileNameLength = fileName.length();
		return fileName.substring(lastIndexOfDot+1, fileNameLength);
	}
	/**
	 * 返回文件名
	 * @param fileName
	 * @return
	 */
	public static String getFileName(String fileName){
		int lastIndexOfDot = fileName.lastIndexOf('.');
		int lastIndexOfSep = fileName.lastIndexOf("/");
		if(!fileName.startsWith("/")){
			lastIndexOfSep = fileName.lastIndexOf("\\");
		}
		lastIndexOfSep = lastIndexOfSep == -1 ? 0 : lastIndexOfSep;
		lastIndexOfDot = lastIndexOfDot == -1 ? fileName.length() : lastIndexOfDot;
		return fileName.substring(lastIndexOfSep, lastIndexOfDot);
	}
	/**
	 * 删除文件夹
	 * @param folderPath 文件夹完整绝对路径
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath);// 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete();// 删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除指定文件夹下所有文件
	 * @param path文件夹完整绝对路径
	 * @return
	 */
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		//文件路径
		String[] tempList = file.list();
		File temp = null;
		for(String tempPath : tempList){
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempPath);
			} else {
				temp = new File(path + File.separator + tempPath);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempPath);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempPath);// 再删除空文件夹
				flag = true;
			}
		}
		//删除最外层文件夹
		if(file.exists()){
			flag = file.delete();
		}
		return flag;
	}
	/**
	 * 项目绝对路径
	 * @param request
	 * @return
	 */
	public static String getRealPath(HttpServletRequest request){
		return request.getSession().getServletContext().getRealPath("/");
	}
	/**
	 * 创建随机文件名
	 * 
	 * @return
	 */
	public static String createRandomName() {
		return DateUtils.formatTime() + getRandomInt();
	}

	/**
	 * 随机数
	 * 
	 * @return
	 */
	public static int getRandomInt() {
		return (new Random()).nextInt(10000);
	}
	/***
	 * 获取图片属性、宽高
	 * @param file
	 * @return 宽高
	 */
	public static String[] getImageAttribute(File file) {
		String [] attribute = new String[2];
		try {
			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(getSuffix(file.getName()));
			ImageReader reader = readers.next();
			ImageInputStream iis = ImageIO.createImageInputStream(file);
			reader.setInput(iis, true);
			attribute[0] = reader.getWidth(0)+"";//图片宽
			attribute[1] = reader.getHeight(0) +"";//图片高
		} catch (IOException e) {
		}
		return attribute;
	}
	/**
	 * 文件拷贝
	 * @param in 输入文件
	 * @param out 输出文件
	 * @throws IOException
	 */
	public static void fileCopy(File in, File out) throws IOException {
		FileInputStream is = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		FileChannel inChannel = is.getChannel();
		FileChannel outChannel = fos.getChannel();
		try {
			// inChannel.transferTo(0, inChannel.size(), outChannel);
			// original -- apparently has trouble copying large files on Windows

			// magic number for Windows, 64Mb - 32Kb)
			int maxCount = (64 * 1024 * 1024) - (32 * 1024);
			long size = inChannel.size();
			long position = 0;
			while (position < size) {
				position += inChannel.transferTo(position, maxCount, outChannel);
			}
		} finally {
			if( inChannel != null){
				inChannel.close();
			}
			if(outChannel != null){
				outChannel.close();
			}
			if (is != null) {
				is.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
	/**
	 * 创建图片缩略图
     * @param filename	文件路径
     * @param thumbWidth	缩略图的宽
     * @param thumbHeight	缩略图的高
     * @param quality	图片质量
     * @param outFilename	输出文件的路径
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws IOException
     */
	public static void createThumbnail(String filename, int thumbWidth, int thumbHeight, int quality, String outFilename)
			throws InterruptedException, FileNotFoundException, IOException {
		// 加载图片
		BufferedImage image = ImageIO.read(new File(filename));
		// 确定缩略图大小的宽度和高度
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		int times = Math.max( Math.max( imageWidth / thumbWidth, 1 ), Math.max( imageHeight / thumbHeight, 1 ));
        ImageUtils.zoomOutImage(filename, outFilename, times);

	}
	/**
	 * 抓屏
	 * @param fileName 保存文件路径
	 * @throws IOException 
	 * @throws AWTException 
	 */
	public static void captureScreen(String fileName) throws IOException, AWTException{

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle screenRectangle = new Rectangle(screenSize);
		Robot robot = new Robot();
		BufferedImage image = robot.createScreenCapture(screenRectangle);
		ImageIO.write(image, "png", new File(fileName));
	}
}