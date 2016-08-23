package com.photo.testphotodor;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailsActivity extends Activity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener {
    private Button ivOriginal, ivSnow, ivSaturation, ivCorner, ivBoost;
    private Button bSave, bShare, bContr, bBack, bFiltres, bBri, bRotate, bSize;
    private LinearLayout llImages, llContrast, llBri;
    private ArrayList<Bitmap> imageStack;
    private Animation animation;
    private static final int BRIGHT_PARAM = 1, CONTRAST_PARAM = 2, ORIGINAL = 3, SNOW = 4,
            CORNER = 5, SATURATION = 6, CORNER_RADIUS = 15, BOOST = 7, SATURATION_LEVEL = 10, BOOST_PER = 200, MIN_SIZE = 60;
    private SeekBar sbContr, sbBri;
    TextView tvContr, tvBri;
    public Bitmap bitmap;
    private String filepath;
    private int countRotation = 0;
    //private  ImageView imageView;
    private PinchImageView imageView;
    private File fn;
    private Bitmap oldImage = null, currentImage = null;
    private int contrastOldValue = 0, brigtnessOldValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);


        llImages = (LinearLayout) findViewById(R.id.llImages);
        llContrast = (LinearLayout) findViewById(R.id.llContrast);
        llBri = (LinearLayout) findViewById(R.id.llBri);


        ivOriginal = (Button) findViewById(R.id.ivOriginal);
        ivSnow = (Button) findViewById(R.id.ivSnow);
        ivSnow.setOnClickListener(this);
        ivSaturation = (Button) findViewById(R.id.ivSaturation);
        ivSaturation.setOnClickListener(this);
        ivCorner = (Button) findViewById(R.id.ivCorner);
        ivCorner.setOnClickListener(this);
        ivBoost = (Button) findViewById(R.id.ivBoost);
        ivBoost.setOnClickListener(this);

        tvContr = (TextView) findViewById(R.id.tvContr);
        tvBri = (TextView) findViewById(R.id.tvBri);

        sbBri = (SeekBar) findViewById(R.id.sbBri);
        sbContr = (SeekBar) findViewById(R.id.sbContr);


        sbContr.setOnSeekBarChangeListener(this);
        sbBri.setOnSeekBarChangeListener(this);

        bBri = (Button) findViewById(R.id.bBri);
        bBri.setOnClickListener(this);

        bSave = (Button) findViewById(R.id.bSave);
        bSave.setOnClickListener(this);

        bShare = (Button) findViewById(R.id.bShare);
        bShare.setOnClickListener(this);

        bBack = (Button) findViewById(R.id.bBack);
        bBack.setOnClickListener(this);

        bRotate = (Button) findViewById(R.id.bRotate);
        bRotate.setOnClickListener(this);

        bSize = (Button) findViewById(R.id.bSize);
        bSize.setOnClickListener(this);

        bFiltres = (Button) findViewById(R.id.bFiltres);
        bFiltres.setOnClickListener(this);

        bContr = (Button) findViewById(R.id.bContr);
        bContr.setOnClickListener(this);

        animation = AnimationUtils.loadAnimation(this, R.anim.translate_anim);


        this.filepath = getIntent().getStringExtra("image").toString();

        bitmap = BitmapFactory.decodeFile(filepath);
        countRotation = MainActivity.getOrientation(filepath);
        /*if(countRotation>0)
        {
            bitmap =  MainActivity.Orientation(filepath,bitmap,orientation);
        }*/


        // this.imageView = (ImageView) findViewById(R.id.image);
        this.imageView = (PinchImageView) findViewById(R.id.image);
        this.imageView.setImageBitmap(bitmap);

        //ivOriginal.setImageBitmap(bitmap);
        this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ivOriginal.setOnClickListener(this);

     /*   Integer[] params = {0, SNOW};
        BMPAsynkTask bmpasynk = new BMPAsynkTask(SNOW);
        bmpasynk.execute(params);

        params = new Integer[]{0, SATURATION};
        BMPAsynkTask saTpasynk = new BMPAsynkTask(SATURATION);
        saTpasynk.execute(params);

        params = new Integer[]{0, CORNER};
        BMPAsynkTask coRpasynk = new BMPAsynkTask(CORNER);
        coRpasynk.execute(params);

        params = new Integer[]{0, BOOST};
        BMPAsynkTask BoostAsynk = new BMPAsynkTask(BOOST);
        BoostAsynk.execute(params);*/

        //----------
        if (savedInstanceState != null) {

            sbBri.setProgress(savedInstanceState.getInt("brProgress"));
            tvBri.setText("Brightness " + savedInstanceState.getInt("brProgress"));

            sbContr.setProgress(savedInstanceState.getInt("contrastProgress"));
            tvContr.setText("Contrast " + savedInstanceState.getInt("contrastProgress"));

            this.brigtnessOldValue=savedInstanceState.getInt("BeforeBrProgress");
            this.contrastOldValue=savedInstanceState.getInt("BeforeContrastProgress");
        }
        this.imageStack = (ArrayList<Bitmap>) getLastNonConfigurationInstance();

        if (imageStack != null) {
            if (imageStack.size() > 0)
                imageView.setImageBitmap(this.imageStack.get(imageStack.size() - 1));
        } else this.imageStack = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onClick(View view) {
        final int rotation = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();

        switch (view.getId()) {
            case R.id.bBri:
                if (rotation == Surface.ROTATION_0) {
                    llImages.setVisibility(LinearLayout.GONE);
                }
                llContrast.setVisibility(LinearLayout.GONE);
                llBri.setVisibility(LinearLayout.VISIBLE);
                // llSetSize.setVisibility(LinearLayout.GONE);

                break;
            case R.id.bFiltres:
                llImages.setVisibility(LinearLayout.VISIBLE);
                llContrast.setVisibility(LinearLayout.GONE);
                llBri.setVisibility(LinearLayout.GONE);

                break;
            case R.id.bContr:
                //при ландшавтной ориентации не прячем миниатюры
                if (rotation == Surface.ROTATION_0) {
                    llImages.setVisibility(LinearLayout.GONE);
                }
                llContrast.setVisibility(LinearLayout.VISIBLE);
                llBri.setVisibility(LinearLayout.GONE);

                break;
            case R.id.bSize:
                //при ландшавтной ориентации не прячем миниатюры
                if (rotation == Surface.ROTATION_0) {
                    llImages.setVisibility(LinearLayout.GONE);
                }
                llContrast.setVisibility(LinearLayout.GONE);
                llBri.setVisibility(LinearLayout.GONE);
                this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                this.imageStack.add(currentImage);

                //Пальцами формируем нужное изображение, увеличиваем/перетягиваем
                //вырезали
                imageView.cutting();
                //показали на экране
                imageView.setImageBitmap(imageView.getCroppedImage(imageView));

                break;
            case R.id.bRotate:
                this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                this.imageStack.add(currentImage);

                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                this.oldImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                Bitmap newBMP = Bitmap.createBitmap(this.oldImage, 0, 0, this.oldImage.getWidth(), this.oldImage.getHeight(), matrix, true); // rotating bitmap

                imageView.setImageBitmap(newBMP);
                break;
            case R.id.bSave:
                try {
                    saveImageToExternal(((BitmapDrawable) imageView.getDrawable()).getBitmap());
                    Toast.makeText(getApplicationContext(), "Image successfully saved!", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Error saving image!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ivOriginal:
                this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                this.imageStack.add(currentImage);

                imageView.setImageBitmap(bitmap);

                break;
            case R.id.ivSnow:
                this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                this.imageStack.add(currentImage);

                imageView.setImageBitmap(ImageEffects.applySnowEffect(((BitmapDrawable) imageView.getDrawable()).getBitmap()));
                break;
            case R.id.bBack:

                // imageView.setImageBitmap(this.currentImage);
                if (imageStack.size() > 0) {

                    imageView.setImageBitmap(imageStack.get(imageStack.size() - 1));
                    imageStack.remove(imageStack.size() - 1);
                }

                this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                break;

            case R.id.ivCorner:
                this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                this.imageStack.add(currentImage);

                imageView.setImageBitmap(ImageEffects.roundCorner(((BitmapDrawable) imageView.getDrawable()).getBitmap(), CORNER_RADIUS));

                break;
            case R.id.ivSaturation:
                this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                this.imageStack.add(currentImage);

                imageView.setImageBitmap(ImageEffects.applySaturationFilter(((BitmapDrawable) imageView.getDrawable()).getBitmap(), SATURATION_LEVEL));

                break;
            case R.id.ivBoost:
                this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                this.imageStack.add(currentImage);

                imageView.setImageBitmap(ImageEffects.boost(((BitmapDrawable) imageView.getDrawable()).getBitmap(), 1, BOOST_PER));

                break;
            case R.id.bShare:

                try {

                   String sharePath=saveImageToExternal(((BitmapDrawable) imageView.getDrawable()).getBitmap());

                    ShareImage(sharePath);

                    File tempFile=new File(sharePath);
                    if(tempFile.exists())
                        tempFile.delete();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                break;

        }
    }

    //при перерисовке єкрана сохраняем стек изображений
    @Override
    public Object onRetainNonConfigurationInstance() {
        Bitmap imageForRotate=((BitmapDrawable) imageView.getDrawable()).getBitmap();
        imageStack.add(imageForRotate);
        return imageStack;
    }

    //Сохранение изображения в директорию
    public String saveImageToExternal(Bitmap bm) throws IOException {

        String imgName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Photoredactor");
        path.mkdirs();
        File imageFile = new File(path, imgName + ".png"); // Imagename.png
        FileOutputStream out = new FileOutputStream(imageFile);
        try {
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
            out.flush();
            out.close();

            MediaScannerConnection.scanFile(this, new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {

                }
            });
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            throw new IOException();

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {

            case R.id.sbContr:
                tvContr.setText(String.valueOf("Contrast : " + seekBar.getProgress()));

                this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                imageStack.add(this.currentImage);

                Integer[] params = {seekBar.getProgress() - this.contrastOldValue, CONTRAST_PARAM};

                BMPAsynkTask myTask = new BMPAsynkTask(CONTRAST_PARAM);
                myTask.execute(params);

                this.contrastOldValue = seekBar.getProgress();

                break;
            case R.id.sbBri:
                this.currentImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                imageStack.add(this.currentImage);

                tvBri.setText(String.valueOf("Brigtness : " + seekBar.getProgress()));

                Integer[] brparams = {seekBar.getProgress() - this.brigtnessOldValue, BRIGHT_PARAM};

                myTask = new BMPAsynkTask(BRIGHT_PARAM);
                myTask.execute(brparams);


                this.brigtnessOldValue = seekBar.getProgress();

                break;
        }
    }

    class BMPAsynkTask extends AsyncTask<Integer, Void, Void> {
        private Bitmap newBMP = null, newOriginalBMP = null;
        private Integer parameter = 0;
        ProgressDialog WaitingDialog = null;

        public BMPAsynkTask(int Param) {
            this.parameter = Param;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
                // Выводим пользователю процесс загрузки
                WaitingDialog = ProgressDialog.show(DetailsActivity.this, "Image processing", "Loading...", true);
        }

        @Override
        protected Void doInBackground(Integer... params) {
            this.parameter = params[1];

            switch (params[1]) {

                case CONTRAST_PARAM:
                    newBMP = ImageEffects.createContrast(currentImage,params[0]);
                    break;
                case BRIGHT_PARAM:
                    newBMP = ImageEffects.createBrightness(currentImage, params[0]);
                    break;
                case SNOW:
                    newBMP = ImageEffects.applySnowEffect(currentImage);

                    break;
                case ORIGINAL:
                    break;
                case SATURATION:
                    newBMP = ImageEffects.applySaturationFilter(currentImage, SATURATION_LEVEL);

                    break;
                case CORNER:
                    newBMP = ImageEffects.roundCorner(currentImage, CORNER_RADIUS);

                    break;
                case BOOST:
                    newBMP = ImageEffects.boost(currentImage, 1, BOOST_PER);

                    break;

            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(newBMP);
// Прячем процесс загрузки
            if (WaitingDialog != null)
                WaitingDialog.dismiss();


        }
    }

    //-------------------------------
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putInt("brProgress", sbBri.getProgress());
        savedInstanceState.putInt("contrastProgress", sbContr.getProgress());
        savedInstanceState.putInt("BeforeBrProgress", this.brigtnessOldValue);
        savedInstanceState.putInt("BeforeContrastProgress", this.contrastOldValue);
        // etc.
        super.onSaveInstanceState(savedInstanceState);
    }

    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    private void ShareImage(String filepath) {
        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);

        // Set the MIME type
        share.setType("image/*");

        // Create the URI from the media
        File media = new File(filepath);
        Uri uri = Uri.fromFile(media);

        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, uri);

        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share to"));
    }
}
