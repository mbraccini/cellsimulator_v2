package utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip
{

	private List<String> fileList;
	private String outputFileZip;
	private String sourceFolder;
	private boolean deleteSourceFolder;

	public Zip(String source, String output, boolean deleteSourceFolder) {
		fileList = new ArrayList<String>();
		sourceFolder = source;
		outputFileZip = output;
		this.deleteSourceFolder = deleteSourceFolder;
	}

	public void removeDir(String dirName) throws IOException{
		Path directory = Paths.get(dirName);
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
		   @Override
		   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		       Files.delete(file);
		       return FileVisitResult.CONTINUE;
		   }

		   @Override
		   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		       Files.delete(dir);
		       return FileVisitResult.CONTINUE;
		   }
		});
	}
	
	public void zip() {
		generateFileList(new File(sourceFolder));
		zipIt(outputFileZip);
		
		if (this.deleteSourceFolder)
			try {
				removeDir(this.sourceFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public void zipIt(String zipFile) {
		byte[] buffer = new byte[1024];
		String source = "";
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try
		{
			try
			{
				source = sourceFolder.substring(sourceFolder.lastIndexOf("\\") + 1, sourceFolder.length());
			}
			catch (Exception e)
			{
				source = sourceFolder;
			}
			fos = new FileOutputStream(zipFile);
			zos = new ZipOutputStream(fos);

			//System.out.println("Output to Zip : " + zipFile);
			FileInputStream in = null;

			for (String file : this.fileList)
			{
				//System.out.println("File Added : " + file);
				ZipEntry ze = new ZipEntry(source + File.separator + file);
				zos.putNextEntry(ze);
				try
				{
					in = new FileInputStream(sourceFolder + File.separator + file);
					int len;
					while ((len = in.read(buffer)) > 0)
					{
						zos.write(buffer, 0, len);
					}
				}
				finally
				{
					in.close();
				}
			}

			zos.closeEntry();
			//System.out.println("Folder successfully compressed");

		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				zos.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void generateFileList(File node)
	{

		// add file only
		if (node.isFile())
		{
			fileList.add(generateZipEntry(node.toString()));

		}

		if (node.isDirectory())
		{
			String[] subNote = node.list();
			for (String filename : subNote)
			{
				generateFileList(new File(node, filename));
			}
		}
	}

	private String generateZipEntry(String file)
	{
		return file.substring(sourceFolder.length() + 1, file.length());
	}
}    