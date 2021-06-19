package com.julian.MainProcess;

import com.julian.EncrypteDecrypt.RSAEncrypt;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Run {
    private final String publicKey;
    private final String privateKey;
    private final String defaultInputFilePath = "./输入内容/";
    private final String defaultFileOutParentPath = "./输出内容/";
    private final String defaultDecryptFileOutParentPath = "./解密内容/";

    public Run() throws Exception {
        System.out.println("\033[0;31;40m " + "* Author: julian(连梓煜) RSA算法不适合加密大文件 *");
        System.out.println("\033[0;31;40m " + "初始化密钥");
        RSAEncrypt.genKeyPair();
        this.publicKey = RSAEncrypt.keyMap.get(0);
        this.privateKey = RSAEncrypt.keyMap.get(1);
        File file = new File(defaultFileOutParentPath);
        if (!file.exists()) {
            System.out.println("\033[0;31;40m " + "文件夹不存在，创建： " + defaultFileOutParentPath + " 完成");
        } else {
            System.out.println(" 删除源文件内容： " + defaultFileOutParentPath);
            this.deleteDir(defaultFileOutParentPath);
        }
        Boolean res = file.mkdirs();
    }

    /**
     * @param path 文件路径
     **/
    public void deleteDir(String path) {
        File file = new File(path);
        if (!file.exists()) {//判断是否待删除目录是否存在
            System.err.println("文件夹不存在");
            return;
        }

        String[] content = file.list();//取得当前目录下所有文件和文件夹
        if (content == null) return;
        for (String name : content) {
            File temp = new File(path, name);
            if (temp.isDirectory()) {//判断是否是目录
                deleteDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
                temp.delete();//删除空目录
            } else {
                if (!temp.delete()) {//直接删除文件
                    System.err.println("Failed to delete " + name);
                }
            }
        }
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    /**
     * @param filePath 文件路径
     **/
    public void encryptFile(String filePath) throws Exception {

    }

    public static void main(String[] args) throws Exception {
        Run run = new Run();

        System.out.println("\033[0;32;40m " + "公钥：" + run.getPublicKey());
        System.out.println("\033[0;33;40m " + "私钥: " + run.getPrivateKey() + "\n");

        System.out.print("\033[0;34;40m " + "请输入加密文件路径(如果输入为空，采用默认文件进行加密)：");
        Scanner scanner = new Scanner(System.in);
        String targetStr = scanner.nextLine();
        if (targetStr.isEmpty()) {
            System.out.println(" 输入内容为空，采用默认路径： " + run.defaultInputFilePath);
            targetStr = run.defaultInputFilePath;
        }

        File file = new File(targetStr);
        if (!file.exists()) {
            System.out.println("\033[0;31;40m " + "文件不存在，退出");
            return;
        }

        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            for (int i = 0; i < Objects.requireNonNull(fileList).length; i++) {
                EncryptSingleFile encryptSingleFile = new EncryptSingleFile(run.publicKey,
                        fileList[i].getAbsolutePath(),
                        run.defaultFileOutParentPath + fileList[i].getName());
                Thread thread = new Thread(encryptSingleFile);
                thread.start();
            }
        } else {
            EncryptSingleFile encryptSingleFile = new EncryptSingleFile(run.publicKey,
                    file.getAbsolutePath(),
                    run.defaultInputFilePath + file.getPath());
            Thread thread = new Thread(encryptSingleFile);
            thread.start();
        }

        System.out.println("\n 请等待... Running...\n");
        Thread.sleep(2000);
        System.out.println("\033[0;35;40m " + "\n 加密文件完成\n");






        System.out.print("\033[0;32;40m " + "请输入解密文件路径(如果输入为空，采用默认文件输出文件进行解密)：");
        String targetInputPath = scanner.nextLine();
        if (targetInputPath.isEmpty()) {
            System.out.println(" 输入内容为空，采用默认路径： " + run.defaultFileOutParentPath);
            targetInputPath = run.defaultFileOutParentPath;
        }

        file = new File(targetInputPath);
        if (!file.exists()) {
            System.out.println(" 输出文件夹不存在，创建： " + targetInputPath);
            file.mkdirs();
        }

        System.out.println("\n 请等待... Running...\n");
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            for (int i = 0; i < Objects.requireNonNull(fileList).length; i++) {
                DecryptSingleFile decryptSingleFile = new DecryptSingleFile(run.privateKey,
                        fileList[i].getPath(),
                        run.defaultDecryptFileOutParentPath + "/" + fileList[i].getName());
                Thread thread = new Thread(decryptSingleFile);
                thread.start();
            }
        } else {
            DecryptSingleFile decryptSingleFile = new DecryptSingleFile(run.privateKey,
                    file.getPath(),
                    run.defaultDecryptFileOutParentPath + "/" + file.getPath());
            Thread thread = new Thread(decryptSingleFile);
            thread.start();
        }

        Thread.sleep(2000);
        System.out.println("\033[0;37;40m " + "\n Finished! Author: julian\n");
    }

}

class EncryptSingleFile implements Runnable {
    private final String publicKey;
    private final String filePath;
    private final String outputFilePath;

    EncryptSingleFile(String publicKey, String filePath, String outputFilePath) {
        this.publicKey = publicKey;
        this.filePath = filePath;
        this.outputFilePath = outputFilePath;
    }

    /**
     * @param str 加密字符串
     **/
    public String encrypt(String str) throws Exception {
        return RSAEncrypt.encrypt(str, this.publicKey);
    }

    @Override
    public void run() {
        System.out.println(" 当前线程： " + Thread.currentThread().getName());
        try {
            Path path = Paths.get(filePath);
            BufferedReader fileIn = null;
            try {
                fileIn = new BufferedReader(new FileReader(path.toString()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedWriter fileOut = new BufferedWriter(new FileWriter(outputFilePath));
            String tempStr;
            while ((tempStr = fileIn.readLine()) != null) {
                fileOut.write(encrypt(tempStr) + '\n');
            }
            fileIn.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(" 线程： " + Thread.currentThread().getName() + " 加密文件" + filePath + "完成，输出文件名：" + outputFilePath);
    }
}

class DecryptSingleFile implements Runnable {
    private final String privateKey;
    private final String inputFilePath;
    private final String outputFilePath;

    DecryptSingleFile(String privateKey, String inputFilePath, String outputFilePath) {
        this.privateKey = privateKey;
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
    }


    /**
     * @param str 解密字符串
     **/
    public String decrypt(String str) throws Exception {
        return RSAEncrypt.decrypt(str, this.privateKey);
    }

    @Override
    public void run() {
        System.out.println(" 当前线程： " + Thread.currentThread().getName());
        try {
            Path path = Paths.get(inputFilePath);
            BufferedReader fileIn = null;
            try {
                fileIn = new BufferedReader(new FileReader(path.toString()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedWriter fileOut = new BufferedWriter(new FileWriter(outputFilePath));
            String tempStr;
            while ((tempStr = fileIn.readLine()) != null) {
                fileOut.write(decrypt(tempStr) + '\n');
            }
            fileIn.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(" 线程： " + Thread.currentThread().getName() + " 解密文件" + inputFilePath + "完成，输出文件名：" + outputFilePath);
    }
}
