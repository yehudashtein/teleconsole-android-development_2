package com.telebroad.teleconsole.helpers;


import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadFile {
        private MultiPartResults multiPartResults;
        private final String boundary = "*****";
        private final String lineEnd = "\r\n";
        private final String twoHyphens = "--";

        public void uploadFile(File file, String secret,String id,MultiPartResults multiPartResults) {
            HttpURLConnection connection;
            DataOutputStream outputStream;
            FileInputStream fileInputStream;
            String urlTo = "https://apiconnact.telebroad.com/v0/file/u/?";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;
            try {
                fileInputStream = new FileInputStream(file);
                URL url = new URL(urlTo);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("x-tinode-apikey", "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo");
                connection.setRequestProperty("x-tinode-auth", "Token " + secret);
                outputStream = new DataOutputStream(connection.getOutputStream());
                //outputStream = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + file.getName() + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"id\"" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(id);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                int serverResponseCode = connection.getResponseCode();
                if (serverResponseCode == 200) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    String inputStr;
                    while ((inputStr = bufferedReader.readLine()) != null) {
                        responseStrBuilder.append(inputStr);
                    }
                    String responseStr = responseStrBuilder.toString();
                    JsonObject jsonObject = JsonParser.parseString(responseStr).getAsJsonObject();
                    multiPartResults.results(jsonObject);
                }
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
       public interface MultiPartResults{
            void results(JsonObject jsonObject);
        }
    public void uploadFile(Uri uri, ContentResolver contentResolver,String id, String secret, MultiPartResults multiPartResults) {
        HttpURLConnection connection;
        DataOutputStream outputStream;
        InputStream inputStream;
        String urlTo = "https://apiconnact.telebroad.com/v0/file/u/?";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        try {
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Unable to open input stream for the given URI");
            }

            URL url = new URL(urlTo);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("x-tinode-apikey", "AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo");
            connection.setRequestProperty("x-tinode-auth", "Token " + secret);
            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            String fileName = getFileName(uri, contentResolver); // method to extract filename from Uri
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            bytesAvailable = inputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = inputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = inputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = inputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"id\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(id);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            int serverResponseCode = connection.getResponseCode();

            if (serverResponseCode == 200) {
                InputStream responseInputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseInputStream, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = bufferedReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }
                String responseStr = responseStrBuilder.toString();
                JsonObject jsonObject = JsonParser.parseString(responseStr).getAsJsonObject();
                multiPartResults.results(jsonObject);
            }

            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getFileName(Uri uri, ContentResolver contentResolver) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    //                        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
//                        httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
//                        Retrofit retrofit = new Retrofit.Builder()
//                                .baseUrl("https://apiconnact.telebroad.com/v0/")
//                                .client(httpClient.build())
//                                .addConverterFactory(GsonConverterFactory.create())
//                                .build();
//                        FileUploadService service = retrofit.create(FileUploadService.class);
//                        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), "121164");
//                        RequestBody fileRequestBody = RequestBody.create(MediaType.parse("video/*"), tempFile);
//                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", tempFile.getName(), fileRequestBody);
//                        Call<ResponseBody> call = service.uploadFile("AQAAAAABAAAuaRVLgV3YdUGRCgwrmyuo", "Token " + secret, id, filePart);
//                        int finalCounter1 = counter;
//                        call.enqueue(new Callback<ResponseBody>() {
//                            @Override
//                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                                String responseBodyString = null;
//                                try {responseBodyString = response.body().string();} catch (IOException e) {e.printStackTrace();}
//                                JsonObject jsonObject = JsonParser.parseString(responseBodyString).getAsJsonObject();
//                                ctrls.add(gson.fromJson(jsonObject.get("ctrl"), CtrlMessage.class));
//                                binding.imgToSend.setVisibility(View.VISIBLE);
//                                DisplayTheMargins();
//                                if (finalCounter1 == extraPhotoURI.size() && ctrls.size() > 0) DisplayImages();
//                            }
//
//                            @Override
//                            public void onFailure(Call<ResponseBody> call, Throwable t) {}
//                        });
}
