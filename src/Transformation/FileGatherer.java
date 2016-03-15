package Transformation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;

/**
 * Created by pip on 03.01.2016.
 */
public class FileGatherer {

    public static ArrayList<VirtualFile> getAllJavaFilesInProject(Project project){
        return getAllJavaFilesInDir(project.getBaseDir());
    }

    public static ArrayList<VirtualFile> getAllJavaFilesInDir(VirtualFile base){
        ArrayList<VirtualFile> allJavaFiles = new ArrayList<VirtualFile>();
        for (VirtualFile child : base.getChildren()){
            if (!child.isDirectory() && child.getExtension() != null && child.getExtension().equalsIgnoreCase("java")){
                allJavaFiles.add(child);
            } else if (child.isDirectory()){
                allJavaFiles.addAll(getAllJavaFilesInDir(child));
            }
        }
        return allJavaFiles;
    }
}
