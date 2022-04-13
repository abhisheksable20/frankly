package com.sakesnake.frankly.home.bottomnavupload;

// Converting uri into bytes
@Deprecated
public class GetImagesDataInParseFile {

//    // Converting uri into byte array
//    public static ParseFile getParseFileFormUri(String pathUri){
//        Date date = new Date();
//        String dateToString = String.valueOf(date.getTime());
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        Bitmap bitmap = ImageResizer.reduceBitmapSize(pathUri,307200);
//        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
//        return new ParseFile(dateToString,outputStream.toByteArray());
//    }
//
//    // Converting list uri into list of byte array
//    public static ArrayList<ParseFile> getListOfImagesInParseFile(List<Uri> imagesPath){
//        Date date = new Date();
//        ArrayList<ParseFile> uriToParseFile = new ArrayList<>();
//        for (int i=0; i<imagesPath.size(); i++){
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            String dateToString = String.valueOf(date.getTime());
//            Bitmap bitmap = ImageResizer.reduceBitmapSize(imagesPath.get(i).getPath(),307200);
//            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
//            uriToParseFile.add(new ParseFile(dateToString,outputStream.toByteArray()));
//        }
//        return uriToParseFile;
//    }
}
