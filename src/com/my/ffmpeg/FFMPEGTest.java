/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.my.ffmpeg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.libffmpeg.ExecuteBinaryResponseHandler;
import com.libffmpeg.FFmpeg;
import com.libffmpeg.LoadBinaryResponseHandler;
import com.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.libffmpeg.exceptions.FFmpegNotSupportedException;

public class FFMPEGTest extends Activity implements OnClickListener {

	private static final String TAG = FFMPEGTest.class.getSimpleName();

	FFmpeg ffmpeg;

	EditText commandEditText;
	Button runButton;
	LinearLayout outputLayout;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ffmpeg_test);

		loadFFMpegBinary();
		initUI();

	}

	private void initUI() {

		commandEditText = (EditText) findViewById(R.id.command);
		runButton = (Button) findViewById(R.id.run_command);
		outputLayout = (LinearLayout) findViewById(R.id.command_output);

		runButton.setOnClickListener(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(null);
	}

	private void loadFFMpegBinary() {
		ffmpeg = FFmpeg.getInstance(this);

		try {
			ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
				@Override
				public void onFailure() {
					showUnsupportedExceptionDialog();
				}
			});
		} catch (FFmpegNotSupportedException e) {
			showUnsupportedExceptionDialog();
		}
	}

	private void execFFmpegBinary(final String[] command) {
		try {
			ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
				@Override
				public void onFailure(String s) {
					addTextViewToLayout("FAILED with output : " + s);
				}

				@Override
				public void onSuccess(String s) {
					addTextViewToLayout("SUCCESS with output : " + s);
				}

				@Override
				public void onProgress(String s) {
					Log.d(TAG, "Started command : ffmpeg " + command);
					addTextViewToLayout("progress : " + s);
					progressDialog.setMessage("Processing\n" + s);
				}

				@Override
				public void onStart() {
					outputLayout.removeAllViews();

					Log.d(TAG, "Started command : ffmpeg " + command);
					progressDialog.setMessage("Processing...");
					progressDialog.show();
				}

				@Override
				public void onFinish() {
					Log.d(TAG, "Finished command : ffmpeg " + command);
					progressDialog.dismiss();
				}
			});
		} catch (FFmpegCommandAlreadyRunningException e) {
			// do nothing for now
		}
	}

	private void addTextViewToLayout(String text) {
		TextView textView = new TextView(FFMPEGTest.this);
		textView.setText(text);
		outputLayout.addView(textView);
	}

	private void showUnsupportedExceptionDialog() {
		new AlertDialog.Builder(FFMPEGTest.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(R.string.device_not_supported))
				.setMessage(getString(R.string.device_not_supported_message))
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								FFMPEGTest.this.finish();
							}
						}).create().show();

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.run_command:
			String cmd = commandEditText.getText().toString();
			String[] command = cmd.split(" ");
			if (command.length != 0) {
				execFFmpegBinary(command);
			} else {
				Toast.makeText(FFMPEGTest.this,
						getString(R.string.empty_command_toast),
						Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

}
