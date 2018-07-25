package elearning.courseprepper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

@SpringBootApplication
public class CourseprepperApplication implements CommandLineRunner {

	@Autowired
	private Environment environment;
	
	public static void main(String[] args) {
		SpringApplication.run(CourseprepperApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		String courseDirectory = environment.getProperty("courseDir");
		System.out.println("Started Processing Course");
		List<String> bundlesList = Arrays.asList(environment.getProperty("courseBundles").split(","));
		for (String bundleName : bundlesList) {
			deleteIgnoreFiles(courseDirectory, bundleName);
			renameAboutFiles(courseDirectory, bundleName);
			createZipFiles(courseDirectory, bundleName);
		}
		System.out.println("Course Processing Finished");
	}

	private void createZipFiles(String courseDirectory, String bundleName) throws Exception {
		File sourceDir = new File(courseDirectory+File.separator+bundleName);
		File zipFile = new File(courseDirectory+File.separator+bundleName+".zip");
		ZipUtil.pack(sourceDir, zipFile);
	}
	
	private void renameAboutFiles(String courseDirectory, String bundleName) throws IOException {
		String PARENT_DIR_NAME = environment.getProperty("renameAboutFilesParentDirName");
		Path courseDirPath = Paths.get(courseDirectory+"\\"+bundleName);
		List<File> filesList = new ArrayList<File>();
		Files.walk(courseDirPath)
		.filter(p -> !Files.isDirectory(p))
		.forEach(p -> {
			String parentDirectoryName = p.getParent().getFileName().toString();
			String fileName = p.getFileName().toString();
			if(PARENT_DIR_NAME.equalsIgnoreCase(parentDirectoryName)){
				if(!fileName.toUpperCase().endsWith(".HTML")){
					if(Files.isRegularFile(p)){
						filesList.add(new File(p.toString()));
					}
				}
			}
		});
		
		for (File file : filesList) {
			file.renameTo(new File(file.getAbsolutePath()+".html"));
		}
		
	}

	private void deleteIgnoreFiles(String courseDirectory, String bundleName) throws IOException {
		List<String> ignoreList =  Arrays.asList(environment.getProperty("ignoreList").split(","));
		Path courseDirPath = Paths.get(courseDirectory+"\\"+bundleName);
		Files.walk(courseDirPath).filter(p -> !Files.isDirectory(p))
		.forEach(p -> {
			String fileName = p.getFileName().toString();
			if(ignoreList.contains(fileName)){
				try {
					Files.delete(p);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
