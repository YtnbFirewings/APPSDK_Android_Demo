package cn.com.broadlink.blappsdkdemo.common;

import android.content.Context;
import android.os.Environment;

import com.alibaba.fastjson.JSON;

import java.io.File;

import cn.com.broadlink.blappsdkdemo.data.DrpDescInfo;
import cn.com.broadlink.sdk.BLLet;

public class BLStorageUtils {
	/**H5 html主页面**/
	public static final String H5_INDEX_PAGE = "app.html";
	/**H5 场景主页面**/
	public static final String H5_CUSTOM_PAGE = "custom.html";
	
	/** APP根目录 **/
	public static String BASE_PATH = "";

	/** APP错误信息保存路径 **/
	public static String CRASH_LOG_PATH = "";

	/** 固件日志保存路径 **/
	public static String FIRMWARE_LOG_PATH = "";

	/** 临时目录 **/
	public static String TEMP_PATH = "";

	/** 缓存路径 **/
	public static String CACHE_PATH = "";

	/** 数据共享路径 **/
	public static String SHARED_PATH = "";

	/** 云空调控制指令路径 **/
	public static String CONCODE_PATH = "";

	/** 家庭路径 存放家庭下的一些信息 **/
	public static String FAMILY_PATH = "";

	/** 设备图标路径 **/
	public static String DEVICE_ICON_PATH = "";

	/** RM设备路径 **/
	public static String IR_DATA_PATH = "";

	/** 场景图标 **/
	public static String SCENE_ICON_PATH = "";

//	/** SDK 解析库存放目录 **/
//	public static String SCRIPTS_PATH = "";

	/** MS1图标路径 **/
	public static String MS1_PATH = "";

	/** 子设备备份路径 **/
	public static String SUB_DEV_BACKUP_PATH = "";

	/** 压力测试日志路径 **/
	public static String STRESS_TEST_LOG_PATH = "";

	/** 离线图标 **/
	public static String OFF_LINE_ICON = "";

//	/** H5页面路径 **/
//	public static String DRPS_PATH = "";

	/** APP缓存数据文件夹 **/
	public static String APP_CACHE_FILE_PATH = "";

	/** 产品列表 **/
	public static String PRODUCT_LIST_PATH = "";

	/** 产品详情 **/
	public static String PRODUCT_INFO_PATH = "";

	/** 隐藏文件夹 存放S1的传感器信息文件 **/
	public static String S1_PATH = "";

	private BLStorageUtils() {}

	public static void init(Context context) {
		String rootPath = null;
		String appPath = null;
		
		// 存在SDCARD的时候，路径设置到SDCARD
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			rootPath = Environment.getExternalStorageDirectory().getPath();
			// 不存在SDCARD的时候，路径设置到ROM
		}else{
			rootPath = context.getCacheDir().getAbsolutePath();
		}

		File dataDir = new File(new File(rootPath, "Android"), "data");
		File appDir = new File(dataDir, context.getPackageName() + File.separator +  "files");
		appDir.mkdirs();

		/********************** 一级目录创建 ********************************/
		BASE_PATH = appPath = appDir.getPath();

		/********************** 二级目录创建 ********************************/
		TEMP_PATH = appPath + "/temp";
		CACHE_PATH = appPath + "/cache";
		
		IR_DATA_PATH = appPath + File.separator + BLConstants.FILE_IR_DATA;
		SUB_DEV_BACKUP_PATH = appPath + File.separator + BLConstants.FILE_SUB_DEV_BACKUP;
		STRESS_TEST_LOG_PATH = appPath + File.separator + BLConstants.FILE_STRESS_TEST_LOG_PATH;
		CRASH_LOG_PATH = appPath + File.separator + BLConstants.FILE_CRASH_LOG_PATH;
		FIRMWARE_LOG_PATH = appPath + File.separator + BLConstants.FILE_FIRMWARE_LOG_PATH;

		/********************** 一级目录 ********************************/
		new File(BASE_PATH).mkdirs();

		/********************** 二级目录 ********************************/
		new File(TEMP_PATH).mkdirs();
		new File(CACHE_PATH).mkdirs();

		new File(IR_DATA_PATH).mkdirs();
		new File(SUB_DEV_BACKUP_PATH).mkdirs();
		new File(STRESS_TEST_LOG_PATH).mkdirs();
		new File(CRASH_LOG_PATH).mkdirs();
		new File(FIRMWARE_LOG_PATH).mkdirs();
	}

	/**
     * 获取产品脚本文件的绝对路径
     * 
     * @param pid
     * 			设备产品id
     * @return
     */
    public static String getScriptAbsolutePath(String pid){
		return BLLet.Controller.queryScriptPath(pid);
    }
 
    /**
     * 获取设备H5主显示页面
     * 
     * @param pid
     * 			设备产品id
     * @return
     */
	public static String getH5IndexPath(String pid) {
		String folderPath = languageFolder(pid);
		if(folderPath != null){
			return folderPath + File.separator + H5_INDEX_PAGE;
		}
		return null;
	}
	
	/**
	 * 获取设备H5 custom.html文件目录
	 *		此HTML可以选择设备的参数用来执行场景或者定时命名
	 *		#创建场景：custom.html?type=scene
	 *		#创建定时：custom.html?type=timer
	 * @param pid
	 * 			设备产品id
	 * @return
	 */
	public static String getH5DeviceParamPath(String pid) {
		String folderPath = languageFolder(pid);
		if(folderPath != null){
			return folderPath + File.separator + H5_CUSTOM_PAGE;
		}
		return null;
	}
	
	/***
	 * 返回语言包的文件夹夹
	 * 	例如 sdcard/broadlink/pid/zh
	 * @param pid
	 * @return
	 */
	public static String languageFolder(String pid){
		String language = BLCommonUtils.getLanguage();
		String folderPath = BLLet.Controller.queryUIPath(pid) + File.separator + language;
		File languageFolder = new File(folderPath);
		if(languageFolder.exists()){
			return folderPath;
		}else{
			String[] languages = language.split("-");
			String countryPath = BLLet.Controller.queryUIPath(pid) + File.separator + languages[0];
			File countryFolder = new File(countryPath);
			if(countryFolder.exists()){
				return countryPath;
			}
		}

		//解析本地desc。json文件，获取默认语言文件夹
		String content = BLFileUtils.readTextFileContent(getH5Folder(pid) + "/desc.json");
		if(content != null){
			DrpDescInfo descInfo = JSON.parseObject(content, DrpDescInfo.class);
			if(descInfo != null){
				return BLLet.Controller.queryUIPath(pid) + File.separator +  descInfo.getDefault_lang();
			}
		}

		return null;
	}
	
	 /**
     * 获取设备H5 文件夹
     * 
     * @param pid
     * 			设备产品id
     * @return
     */
	public static String getH5Folder(String pid) {
		return BLLet.Controller.queryUIPath(pid);
	}

}
