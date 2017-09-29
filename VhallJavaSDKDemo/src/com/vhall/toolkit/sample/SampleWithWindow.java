package com.vhall.toolkit.sample;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;

import org.apache.http.util.TextUtils;
import org.json.JSONObject;

import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.aliyun.oss.model.Callback;
import com.aliyun.oss.model.Callback.CalbackBodyType;
import com.vhall.toolkit.VhallUploadKit;

public class SampleWithWindow extends JFrame {

	// 编辑部分
	public static final String APP_KEY = "";
	public static final String SECRET_KEY = "";
	public static final String videoName = "测试 & 回放 名称";
	public static final String objectName = "测试 * & % ￥活动 名称";
	public static final String callbackurl = "http://t.e.vhall.com/api/callback";

	// demo
	static VhallUploadKit util;
	static Callback callback;
	static File file;
	static JLabel fileLabel;
	static JLabel tipsLabel;
	static JProgressBar bar;
	public static String file_oss_url = "";

	public SampleWithWindow() {
		util = VhallUploadKit.getInstance();
		callback = new Callback();
		callback.setCallbackUrl(callbackurl);
		callback.setCallbackBody("{\\\"mimeType\\\":${mimeType},\\\"size\\\":${size}}");
		callback.setCalbackBodyType(CalbackBodyType.JSON);
	}

	public static void main(String[] args) {

		SampleWithWindow window = new SampleWithWindow();
		window.setTitle("vhall upload kit");
		window.setSize(600, 300);
		window.setResizable(false);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setContentPane(initUI());
		window.setVisible(true);

		util.initData(APP_KEY, SECRET_KEY);
		if (util.isEnable()) {
			tipsLabel.setText("初始化成功！");
		} else {
			tipsLabel.setText("初始化失败！");
		}

	}

	private static JPanel initUI() {
		final JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(10, 1));
		fileLabel = new JLabel();
		fileLabel.setText("请选择文件！");
		fileLabel.setHorizontalAlignment(SwingConstants.CENTER);
		bar = new JProgressBar();
		bar.setMaximum(100);
		bar.setMinimum(0);
		bar.setValue(0);
		bar.setStringPainted(true);

		JButton selectBtn = new JButton("选择文件");
		selectBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectFile(contentPanel);
			}
		});
		JButton uploadBtn = new JButton("上传文件");
		uploadBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (util.isEnable() && file != null)
					file_oss_url = util.uploadFile(file, callback, new PutObjectProgressListener());
			}
		});
		JButton createBtn = new JButton("生成回放");
		createBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(file_oss_url)) {
					String result = util.createWebinar(APP_KEY, SECRET_KEY, videoName, objectName, file_oss_url);
					if (!TextUtils.isEmpty(result)) {
						JSONObject obj = new JSONObject(result);
						String webinarid = obj.optString("webinar_id");
						String records_id = obj.optString("records_id");
						tipsLabel.setText("生成回放成功,活动ID：" + webinarid + ",回放ID：" + records_id);
					} else
						tipsLabel.setText("生成回放失败！");
				}
			}
		});
		tipsLabel = new JLabel();
		tipsLabel.setText("初始化，请稍等...");
		tipsLabel.setHorizontalAlignment(SwingConstants.CENTER);

		contentPanel.add(new JLabel());
		contentPanel.add(fileLabel);
		contentPanel.add(new JLabel());
		contentPanel.add(bar);
		contentPanel.add(selectBtn);
		contentPanel.add(uploadBtn);
		contentPanel.add(createBtn);
		contentPanel.add(new JLabel());
		contentPanel.add(tipsLabel);
		contentPanel.add(new JLabel());

		return contentPanel;
	}

	/**
	 * 获取上传进度回调
	 */
	static class PutObjectProgressListener implements ProgressListener {

		private long bytesWritten = 0;
		private long totalBytes = -1;
		private boolean succeed = false;

		@Override
		public void progressChanged(ProgressEvent progressEvent) {
			long bytes = progressEvent.getBytes();
			ProgressEventType eventType = progressEvent.getEventType();
			switch (eventType) {
			case TRANSFER_STARTED_EVENT:
				tipsLabel.setText("Start to upload......");
				break;
			case REQUEST_CONTENT_LENGTH_EVENT:
				this.totalBytes = bytes;
				break;
			case REQUEST_BYTE_TRANSFER_EVENT:
				this.bytesWritten += bytes;
				if (this.totalBytes != -1) {
					int percent = (int) (this.bytesWritten * 100.0 / this.totalBytes);
					bar.setValue(percent);
					System.out.println(bytes + " bytes have been written at this time, upload progress: " + percent
							+ "%(" + this.bytesWritten + "/" + this.totalBytes + ")");
				} else {
					// bar.setValue(100);
					System.out.println(bytes + " bytes have been written at this time, upload ratio: unknown" + "("
							+ this.bytesWritten + "/...)");
				}
				break;

			case TRANSFER_COMPLETED_EVENT:
				this.succeed = true;
				tipsLabel.setText("Succeed to upload!");
				// System.out.println("Succeed to upload, " + this.bytesWritten
				// + " bytes have been transferred in total");
				break;

			case TRANSFER_FAILED_EVENT:
				tipsLabel.setText("Failed to upload!");
				// System.out.println("Failed to upload, " + this.bytesWritten +
				// " bytes have been transferred");
				break;

			default:
				break;
			}
		}

		public boolean isSucceed() {
			return succeed;
		}
	}

	private static void selectFile(Component parent) {
		int result = 0;
		JFileChooser fileChooser = new JFileChooser();
		FileSystemView fsv = FileSystemView.getFileSystemView(); // 注意了，这里重要的一句
		System.out.println(fsv.getHomeDirectory()); // 得到桌面路径
		fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
		fileChooser.setDialogTitle("请选择要上传的文件...");
		fileChooser.setApproveButtonText("确定");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		result = fileChooser.showOpenDialog(parent);
		if (JFileChooser.APPROVE_OPTION == result) {
			file = new File(fileChooser.getSelectedFile().getPath());
			fileLabel.setText("待上传文件：" + file.getAbsolutePath());
		}
	}

}
