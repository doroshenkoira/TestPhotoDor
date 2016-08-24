package com.photo.testphotodor;

import android.graphics.Bitmap;

class ImageItem {
  //  private Bitmap image;

    private String filePath;

    public ImageItem(Bitmap image, String filePath) {
        super();
      //  this.image = image;

        this.filePath=filePath;
    }
   // public Bitmap getImage() {
     //   return image;
   // }

   // public void setImage(Bitmap image) {
   //     this.image = image;
   // }

    public String getfilePath() {
        return filePath;
    }

}

