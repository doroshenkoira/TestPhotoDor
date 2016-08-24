package com.photo.testphotodor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {
    private GridView gridView;
    private GridViewAdapter gridAdapter;

    //результаты для фото
    public static final int MEDIA_TYPE_IMAGE = 1, MEDIA_TYPE_VIDEO = 2;
    private ArrayList<ImageItem> datas = null;
    private ArrayList<ImageItem> imageItems = null;
    private Button bGetFromCamera, button_capture;
   //фрейм для Камеры
    private FrameLayout preview = null;

    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {

            mCamera = getCameraInstance();
            button_capture = (Button) findViewById(R.id.button_capture);

            // Create our Preview view and set it as the content of our activity.
            preview = (FrameLayout) findViewById(R.id.camera_preview);

//кнопка получения фото
            button_capture.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera
                            mCamera.takePicture(null, null, mPicture);
                        }
                    }
            );

            gridView = (GridView) findViewById(R.id.gridView);
            bGetFromCamera = (Button) findViewById(R.id.bGetFromCamera);
            bGetFromCamera.setOnClickListener(this);

//получаем списое изображений
            datas = getItemsList();
            //инициализация Адаптера
            gridAdapter = new GridViewAdapter(MainActivity.this, R.layout.grid_item_layout, datas);
            gridView.setAdapter(gridAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                    ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                    createIntentAndStart("image", item.getfilePath());
                }
            });
        } catch (Exception ex) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Ошибка загрузки приложения",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
//запуск Активити Редактора
    private void createIntentAndStart(String text, String pathContent) {
        Intent i = new Intent(MainActivity.this, DetailsActivity.class);

        i.putExtra(text, pathContent);

        startActivity(i);
    }
    // готовим єлементы для GridView

    //получаем список всех изображений устройства
    public ArrayList<String> getAllShownImagesPath(Activity activity) throws IOException {
        try {
            Uri uri;
            Cursor cursor;
            int column_index_data, column_index_folder_name;
            ArrayList<String> listOfAllImages = new ArrayList<String>();

            String absolutePathOfImage;

            uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

            cursor = activity.getContentResolver().query(uri, projection, null,
                    null, null);

            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            column_index_folder_name = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data);


                listOfAllImages.add(absolutePathOfImage);
            }
            return listOfAllImages;
        }
        catch (Exception ex)
        {
            return null;
        }
    }
//разворот готового изображения
    public static Bitmap Orientation(String path, Bitmap bmpImage, int orientation) {
        //----------------------------------------
        try {
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }

            Bitmap newBmpImage = Bitmap.createBitmap(bmpImage, 0, 0, bmpImage.getWidth(), bmpImage.getHeight(), matrix, true); // rotating bitmap
            return newBmpImage;
        } catch (Exception e) {
            return null;
        }
        //---------------------------------------
    }
//сохраняем данные при перерисовке
    @Override
    public Object onRetainNonConfigurationInstance() {
        return imageItems;
    }

    //получаем реальную ориентацию изображения
    public static int getOrientation(String imagePath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        return orientation;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("ERROR", "On Resume");
        datas = imageItems;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("ERROR", "On Pause");
        releaseCamera();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bGetFromCamera:
                //запускаем Камеру
                mPreview = new CameraPreview(this, mCamera);

                button_capture.setVisibility(View.VISIBLE);
                preview.addView(mPreview);

                break;
        }
    }
//получаем список всех изображений
    private ArrayList<ImageItem> getItemsList() {
        imageItems = new ArrayList<>();
        ArrayList<String> imgs = null;
        try {
            imgs = getAllShownImagesPath(MainActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < imgs.size(); i++) {

            imageItems.add(new ImageItem(null, imgs.get(i)));

        }
        return imageItems;
    }
//получаем обьект Камера
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

 //Create a file Uri for saving an image or video

    private Uri getOutputMediaFileUri(int type) throws FileNotFoundException {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) throws FileNotFoundException {

        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Camera");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Создаем файл из фотокамеры
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".png");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;

    }
//получаем фото из фотокамеры и сохраняем его
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = null;
            try {
                pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (pictureFile == null) {
                Log.d("CAMERA ", "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                Bitmap bmp;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);


                FileOutputStream out = new FileOutputStream(pictureFile);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
                out.flush();
                out.close();
                MediaScannerConnection.scanFile(MainActivity.this, new String[]{pictureFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
                } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {

                }
                createIntentAndStart("image", pictureFile.getPath());

        }
    };
}

