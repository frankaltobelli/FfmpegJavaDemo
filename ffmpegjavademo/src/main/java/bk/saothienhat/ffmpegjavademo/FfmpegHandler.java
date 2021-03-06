/*
 * Copyright (c) 2017 Sao Thien Hat. All rights reserved.
 */
package bk.saothienhat.ffmpegjavademo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import bk.saothienhat.ffmpegjavademo.model.FFMPEGCommandType;
import bk.saothienhat.ffmpegjavademo.model.FFMPEGOptionType;
import bk.saothienhat.ffmpegjavademo.model.FFMPEGRespond;
import bk.saothienhat.ffmpegjavademo.model.FfmpegCommand;
import bk.saothienhat.ffmpegjavademo.model.FileType;
import bk.saothienhat.ffmpegjavademo.utils.Logger;

/**
 * @author Sao Thien Hat
 *
 */
public class FfmpegHandler {
    
    private String ffmpegPath;
    private int logLevel;
    private long exeTime; // (seconds)
    
    
    /**
     * Method to get field ffmpegPath as type String
     * @return the ffmpegPath
     */
    public String getFfmpegPath() {
        return ffmpegPath; 
    }
    
    /**
     * Method to set value for field ffmpegPath
     *
     * @param ffmpegPath the ffmpegPath to set
     */
    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }
    
    public void setExeTime(long value){
        this.exeTime = value;
    }
    
    public long getExeTime(){
        return this.exeTime;
    }
    
    
    /**
     * Construct new instance of FfmpegHandler
     */
    public FfmpegHandler() {
        initialize();        
    }

    /**
     * Method to initialize
     *
     */
    private void initialize() {
        this.logLevel = FfmpegCommand.LOGLEVEL_ALL;
    }

    /**
     * Method to convert video basically with convert command: ffmpeg -i your_input_file your_output_file
     *
     * @param inputFilePath
     * @param outputFilePath
     */
    public FFMPEGRespond convertVideoBasic(String inputFilePath, String outputFilePath){   
        Logger.log("FfmpegHandler.convertVideoBasic(): start converting...");
        FFMPEGRespond ffmpegRespond = convert(FFMPEGOptionType.I.getOption(), inputFilePath, FFMPEGOptionType.EMPTY.getOption(), outputFilePath);
        return ffmpegRespond;
    }
    
    
    /**
     * Method to convert generally using FFMPE
     * Syntax: FFMPEG <option> your_input_file <convert_option> your_output_file
     * Example: 
     *          ffmpeg -i input.mp4 output.avi
     *          ffmpeg -i video.flv -vn -ar 44100 -ac 2 -ab 192 -f mp3 audio.mp3
     *
     * @param option
     * @param inputFilePath
     * @param convertOption
     * @param outputFilePath
     */
    public FFMPEGRespond convert(String option, String inputFilePath, String convertOption, String outputFilePath) {
        String exeCommand = getFfmpegPath() + " " + option + " " + inputFilePath + " " + convertOption + " " + outputFilePath;
        Logger.log("FfmpegHandler.convertVideoBasic(): start converting with Convert Info: ");
        Logger.log("\tInput:  " + inputFilePath);
        Logger.log("\tOutput: " + outputFilePath);
        Logger.log("\tOption:  " + option);
        Logger.log("\tConvert Option:  " + convertOption);
        
        FfmpegCommand command = new FfmpegCommand(exeCommand, FFMPEGCommandType.CONVERT_MEDIA_FILE);        
        FFMPEGRespond ffmpegRespond = execute(command);
        return ffmpegRespond;
    }
    
    /**
     * Method to execuse a certain FFMPEG command
     *
     * @param command
     */
    private FFMPEGRespond execute(FfmpegCommand command){ 
        FFMPEGRespond ffmpegRespond = new FFMPEGRespond();
        List<String> errorMessages = new ArrayList<String>();
        boolean isLogLevelErrorOnly = ( getLogLevel() == FfmpegCommand.LOGLEVEL_ERROR );
        try {
            String exeCommand = command.getCommand();
            Logger.log("FfmpegHandler.execuse(): @@==============  Begin execute FFMPEG command: " + exeCommand);
            
            // Run FFMPEG command            
            Process process = Runtime.getRuntime().exec(exeCommand);            
            
            /*
             * Get execution process info
             */
            InputStream stderr = process.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = "";
            
            while( (line = br.readLine()) != null ) {
                // Logger.log("FfmpegHandler.execuse(): Running " + command.getCommandType().getCommandDescription() + " ......................");
                Logger.log("\tExecusion output: " + line);
                if(isLogLevelErrorOnly) {
                    errorMessages.add(line);
                }
            }
                         
            
            int exitVal = process.waitFor();
            Logger.log("FfmpegHandler.execuse(): Execute FFMPEG command Finished !!!!! Process exitValue: " + exitVal);
            if( exitVal == 0 ) {
                // Successful
               ffmpegRespond.setExecutionResult(ffmpegRespond.EXE_SUCCESS);
            }else{
                // Fail/Error
                ffmpegRespond.setExecutionResult(ffmpegRespond.EXE_FAIL);
                ffmpegRespond.setErrors(errorMessages);
            }
            
            Logger.log("FfmpegHandler.execuse(): @@==============  Execute FFMPEG command DONE ======================@@");
        }
        catch( IOException e ) {            
            Logger.error(e.getMessage());            
            errorMessages.add(e.getMessage());
            ffmpegRespond.setExecutionResult(ffmpegRespond.EXE_FAIL);
            ffmpegRespond.setErrors(errorMessages);
        }
        catch( InterruptedException e ) {
            Logger.error(e.getMessage());
            errorMessages.add(e.getMessage());
            ffmpegRespond.setExecutionResult(ffmpegRespond.EXE_FAIL);
            ffmpegRespond.setErrors(errorMessages);
        }
        
        return ffmpegRespond;
    }

    
    /**
     * Method to get Video file information
     *
     * @param inputFilePath
     * @param isHideBanner: The -hide_banner option is used to hide a copyright notice shown my FFMPEG, 
     *      such as build options and library versions. This option can be used to suppress printing this information.
     */
    public FFMPEGRespond getVideoFileInfo(String inputFilePath, boolean isHideBanner) {
        Logger.log("FfmpegHandler.getVideoFileInfo(): get file info of Video file: " + inputFilePath);
        String hideBannerStr = (isHideBanner) ? (" " + FFMPEGOptionType.HIDE_BANNER.getOption()) : "";
        String exeCommand = getFfmpegPath() + " " + FFMPEGOptionType.I.getOption() + " " + inputFilePath + hideBannerStr;
        
        FfmpegCommand command = new FfmpegCommand(exeCommand, FFMPEGCommandType.COMPRESS_MEDIA_FILE);
        FFMPEGRespond ffmpegRespond = execute(command);
        return ffmpegRespond;
    }

    /**
     * Method to convertAndCompressVideoBasic
     *
     * @param inputFilePath
     * @param outputFilePath
     */
    public void compressMediaFile(FileType fileType, String inputFilePath, String outputFilePath) {
        Logger.log("FfmpegHandler.compressMediaFile(): : " + fileType.getFileType());
        String exeCommand = "";        
        
        if(FileType.AVI.getFileType().equalsIgnoreCase(fileType.getFileType())){
            // ffmpeg -i input.avi -vcodec msmpeg4v2 output.avi
            exeCommand = getFfmpegPath() + " " + FFMPEGOptionType.I.getOption() + " " + inputFilePath + " -vcodec msmpeg4v2 " + outputFilePath;
        } else if(FileType.MP4.getFileType().equalsIgnoreCase(fileType.getFileType())){
            // ffmpeg -i input.mp4 -acodec mp2 output.mp4
            exeCommand = getFfmpegPath() + " " + FFMPEGOptionType.I.getOption() + " " + inputFilePath + " -acodec mp2 " + outputFilePath;
        }
        
        if(!exeCommand.isEmpty()){
            FfmpegCommand command = new FfmpegCommand(exeCommand, FFMPEGCommandType.COMPRESS_MEDIA_FILE);
            execute(command);
        }
        
    }

    /**
     * Method to get field logLevel as type int
     * @return the logLevel
     */
    public int getLogLevel() {
        return logLevel;
    }

    /**
     * Method to set value for field logLevel
     *
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }
    
    
    
    

}
