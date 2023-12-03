package com.ptn.test_drawing;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ptn.test_drawing.itemL.CustomGridAdapter;
import com.ptn.test_drawing.itemL.CustomListAdapter;
import com.ptn.test_drawing.itemL.Item_draw;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import yuku.ambilwarna.AmbilWarnaDialog;


public class ActivityPaint extends AppCompatActivity {

    private DrawView paint;

    private ImageView btnUndo, btnRedo, btnColor, btnPen, btnMenu, btnNew, btnFullScreenHide, btnFullScreenShow;

    LinearLayout layoutMenu;

    int DefaultColor = Color.BLACK;

    GridView gridView;
    ListView listView;
    LinearLayout layoutSizeAndOpacity;

    SeekBar seekBarSize, seekBarOpacity;
    TextView txtCountSize, txtCountOpacity;

    String ip;
    int port;

    private ProgressDialog progressDialog;


    private List<Item_draw> getListData() {
        List<Item_draw> list = new ArrayList<Item_draw>();
        Item_draw save = new Item_draw("save");
        Item_draw shapes = new Item_draw("shapes");
        Item_draw eraser = new Item_draw("eraser");
        Item_draw text = new Item_draw("text");
        Item_draw importImage = new Item_draw("image");
        Item_draw exit = new Item_draw("exit");

        list.add(save);
        list.add(importImage);
        list.add(shapes);
        list.add(eraser);
        list.add(text);
        list.add(exit);

        return list;
    }


    private List<Item_draw> getListDataList() {
        List<Item_draw> list = new ArrayList<Item_draw>();
        Item_draw save = new Item_draw("save");
        Item_draw discard = new Item_draw("discard");

        list.add(save);
        list.add(discard);

        return list;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hiện thị ứng dụng full màn hình
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_paint);

        layoutMenu = findViewById(R.id.layoutMenu);
        // getting the reference of the views from their ids
        paint = findViewById(R.id.draw_view);
        btnRedo = findViewById(R.id.btnRedo);
        btnUndo = findViewById(R.id.btnUndo);
        btnColor = findViewById(R.id.btnColor);
        btnMenu = findViewById(R.id.btnMenu);
        btnPen = findViewById(R.id.btnPen);
        btnNew = findViewById(R.id.btnNew);
        btnFullScreenHide = findViewById(R.id.btnFullScreenHide);
        btnFullScreenShow = findViewById(R.id.btnFullScreenShow);
        seekBarSize = findViewById(R.id.seekBarSize);
        seekBarOpacity = findViewById(R.id.seekBarOpacity);
        txtCountSize = findViewById(R.id.txtCountSize);
        txtCountOpacity = findViewById(R.id.txtCountOpacity);
        layoutSizeAndOpacity = findViewById(R.id.layoutSizeAndOpacity);
        gridView = findViewById(R.id.gridView);
        listView = (ListView) findViewById(R.id.listView);
        paint.setObjectInActivity(gridView, listView, layoutSizeAndOpacity, btnUndo, btnRedo);


        layoutMenu.bringToFront();

        btnUndo.setEnabled(false);
        btnRedo.setEnabled(false);

        Intent intent = getIntent();
        ip = intent.getStringExtra("ip_key");
        port = intent.getIntExtra("port_key", 6862);

        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paint.undo();
            }
        });
        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paint.redo();
            }
        });


        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenColorPickerDialog(false);
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHide(v);
            }
        });

        List<Item_draw> image_details = getListData();
        gridView.setAdapter(new CustomGridAdapter(this, image_details));

        // When the user clicks on the GridItem
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = gridView.getItemAtPosition(position);
                Item_draw itemdraw = (Item_draw) o;
                switch (position) {
                    case 0: // Save
                        btnSaveImage(v);
                        break;
                    case 1: // Import Image
                        btnOpenImage(v);
                        break;
                    case 2: // Shapes
                        break;
                    case 3: // Eraser
                        break;
                    case 4: // Text
                        paint.addSticker();
                        break;
                    case 5: // Exit
                        btnLogout(v);
                        break;

                }
            }
        });

        List<Item_draw> itemList = getListDataList();
        listView.setAdapter(new CustomListAdapter(this, itemList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = listView.getItemAtPosition(position);
                switch (position) {
                    case 0: // Save
                        btnSaveImage(v);
                        break;
                    case 1: // Discard
                        showAlertDialog();
                        break;
                }
            }
        });


        btnFullScreenHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMenu.setVisibility(View.GONE);
                btnFullScreenShow.setVisibility(View.VISIBLE);
                btnFullScreenShow.bringToFront();
            }
        });

        btnFullScreenShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMenu.setVisibility(View.VISIBLE);
                btnFullScreenShow.setVisibility(View.GONE);
            }
        });


        ViewTreeObserver vto = paint.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                paint.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = paint.getMeasuredWidth();
                int height = paint.getMeasuredHeight();
                paint.init(height, width, ip, port);
            }
        });

        txtCountSize.setText(seekBarSize.getProgress() + "");
        txtCountOpacity.setText(seekBarOpacity.getProgress() + "");

        seekBarSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtCountSize.setText(progress + "");
                paint.setStrokeWidth(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtCountOpacity.setText(progress + "");
                paint.setAlpha(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        btnPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutSizeAndOpacity.getVisibility() == View.VISIBLE) {
                    layoutSizeAndOpacity.setVisibility(View.GONE);
                } else {
                    layoutSizeAndOpacity.setVisibility(View.VISIBLE);
                    layoutSizeAndOpacity.bringToFront();
                }
            }
        });

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listView.getVisibility() == View.VISIBLE) {
                    listView.setVisibility(View.GONE);
                } else {
                    listView.setVisibility(View.VISIBLE);
                    listView.bringToFront();
                }
            }
        });

    }

    public void btnOpenImage(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                // Lấy ảnh từ Gallery
                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                paint.open(selectedBitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void btnImportText(View view) {
        paint.addSticker();
    }

    public void btnLogout(View view) {
        //sendData_v1("logout");
        Intent intent = new Intent(this, ConnectToTheServerActivity.class);
        startActivity(intent);
        finish();
    }

    public void btnSaveImage(View view) {
        // Tạo và hiển thị ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang lưu ảnh...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        Uri images;
        Bitmap bitmap = paint.save();
        ContentResolver contentResolver = getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(images, contentValues);
        try {
            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);
        } catch (Exception e) {
            Toast.makeText(this, "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        finally {
            // Đóng ProgressDialog sau một khoảng thời gian
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    paint.newImage();
                }
            }, 1000);
        }
    }

    private void showAlertDialog() {
        // Tạo một đối tượng AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Thiết lập tiêu đề và thông điệp cho thông báo
        builder.setTitle("Discard current Sketch");
        builder.setMessage("You will lose your sketch if you discard it. Are you sure you want to discard?");

        // Thiết lập nút OK và hành động khi nút đó được nhấn
        builder.setPositiveButton("DISCARD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Hành động khi nút OK được nhấn
                paint.newImage();            }
        });
        // Thiết lập nút No và hành động khi nút đó được nhấn
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Hành động khi nút No được nhấn
                dialog.dismiss();
            }
        });


        // Tạo và hiển thị AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // Hàm ẩn hiện gridview
    public void showHide(View view) {
        if (gridView.getVisibility() == View.VISIBLE) {
            gridView.setVisibility(View.GONE);
        } else {
            gridView.setVisibility(View.VISIBLE);
            gridView.bringToFront();
        }
    }


    private void OpenColorPickerDialog(boolean AlphaSupport) {

        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(ActivityPaint.this, DefaultColor, AlphaSupport, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog ambilWarnaDialog, int color) {
                paint.setColor(color);
            }

            @Override
            public void onCancel(AmbilWarnaDialog ambilWarnaDialog) {

                Toast.makeText(ActivityPaint.this, "Color Picker Closed", Toast.LENGTH_SHORT).show();
            }
        });
        ambilWarnaDialog.show();

    }
}
