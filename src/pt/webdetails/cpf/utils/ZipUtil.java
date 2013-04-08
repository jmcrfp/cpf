/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cpf.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.ResultFile;


/**
 *
 * @author bandjalah
 */
public class ZipUtil {
    private String zipName;
    private String zipPath;
    private String zipFullPath;
    private ZipOutputStream zipOut;
    private FileInputStream fis;
    private FileName topFilename;
    ArrayList<String> fileListing = new ArrayList<String>();
    
    protected Log logger = LogFactory.getLog(this.getClass());
    
    public ZipUtil(){
        //Suposed to be empty
    }
    
    public void buildZip(List<ResultFile> filesList){
        try {
            
            topFilename = getTopFileName(filesList);
            zipName = this.topFilename.getBaseName();
            File tempZip = File.createTempFile(zipName, ".tmp");
            zipFullPath = zipPath+zipName;
            
            FileOutputStream fos = new FileOutputStream(tempZip);
            zipOut = new ZipOutputStream(fos); 
            
            fis = null;

            logger.info("Building '"+zipFullPath+"'...");

            writeEntriesToZip(filesList);
            logger.info("'"+zipName+"' built."+" Sending to client...");
            zipOut.close();
            fos.close();

            fis = new FileInputStream(tempZip);
                
                
        } catch (Exception ex) {
            Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void writeEntriesToZip(List<ResultFile> filesList){
        int i=0;
        try {
            for (ResultFile resFile : filesList) {
                i++;
                logger.info("Files to process:"+filesList.size());
                logger.info("Files processed: "+i);
                logger.info("Files remaining: "+(filesList.size()-i));
                logger.debug(resFile.getFile().getName().getPath());
                FileObject myFile = resFile.getFile();
                
                fileListing.add(removeTopFilenamePathFromString(myFile.getName().getPath()));

                ZipEntry zip = new ZipEntry(removeTopFilenamePathFromString(myFile.getName().getPath()));
                zipOut.putNextEntry(zip);

                byte[] bytes = IOUtils.toByteArray(myFile.getContent().getInputStream());

                zipOut.write(bytes);
                zipOut.closeEntry();
            }
            
            
        } catch (Exception exception) {
            logger.error(exception);
        }
    }

    public FileInputStream getZipInputStream(){
        return fis;
    }
    
    
    public String getZipNameToDownload(){
        return getZipName().replaceAll(" ", "-")+".zip"; //Firefox and Opera don't interpret blank spaces and cut the string there causing the files to be interpreted as "bin".
    }
    
    public String getZipName(){
        return zipName;
    }
    
    public int getZipSize(){
        return 0;
    }
    
    private FileName getTopFileName(List<ResultFile> filesList){
        FileName topFileName = null;
        try {
            if (!filesList.isEmpty()){
                topFileName =  filesList.get(0).getFile().getParent().getName();
            } 
            for (ResultFile resFile : filesList) {
                logger.debug(resFile.getFile().getParent().getName().getPath());
                FileName myFileName = resFile.getFile().getParent().getName();               
                if ( topFileName.getURI().length() > myFileName.getURI().length() ){
                    topFileName = myFileName;
                }           
            }            
        } catch (Exception exception) {
            logger.error(exception);
        }     
        return topFileName;
    }
    
    private String removeTopFilenamePathFromString(String path){
        
        String filteredPath = null;
        int index = this.topFilename.getParent().getPath().length();
        filteredPath = path.substring(index);
        
        
        return filteredPath;
    }
}
