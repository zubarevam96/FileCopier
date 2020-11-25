# FileCopier
FileCopier is a simple program for Windows that helps when you need to (re)place some recently updated files from one directory and it's subdirectories to another for many times. Old files will be backuped.
# What does it do
When launched, There are TrayIcon appears in your SystemTray: ![there must be the program tray icon, but something gone wrong :(](./FilesCopier/src/main/images/copyPaste.png). Upon right-clicking on it, there are few menu buttons:
- execute. Checks inputDirectory and it's subdirectories recursively for recently updated files and, if there are some, puts it to outputfolder with exact relative path;
in case of name collision, old files gets extension ".bak*datetime*" (like: filename.jpg.back20040815_16h23m42s)
- configure
  - set input directory. Defines root path to directory which recently updated files must be copied from
  - set output directory. Defines root path to directory recently updated files must be copied to
  - set min file's age. Program will ignore files that wasn't created/updated past <*this number*> hours; technically, it defines "recency" of files in hours
  - open in explorer. Opens directory with Properties of file in your explorer
- exit. Exits the program<br>
Program have lazy properties reading: it reads the file *C:\Program Files\zamSoft\FilesCopier\FilesCopier.properties* every time before putting some changes in it or execution, so you can easily change it manually even when program is running, and it will apply this changes. **Warning: all not [latin-1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) symbols in properties file must be replaced by krakozyabras like "\u0417" because [Properties.store documentation](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/Properties.html#load(java.io.InputStream))***
# On first launch
After first launch and trying to set somewhat, the program creates directory "%PROGRAMFILES(x86)%/zamSoft/FilesCopier" with "FilesCopier.propersies" in it.
