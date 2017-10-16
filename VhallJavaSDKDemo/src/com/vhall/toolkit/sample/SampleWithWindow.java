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
import javax.swing.filechooser.FileSystemView;

import org.apache.http.util.TextUtils;
import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.aliyun.oss.model.Callback;
import com.aliyun.oss.model.Callback.CalbackBodyType;
import com.vhall.toolkit.VhallUploadKit;

public class SampleWithWindow extends JFrame {
	private static final long serialVersionUID = 560684569647135515L;
	// 编辑部分
	public static final String APP_KEY = "";
	public static final String SECRET_KEY = "";
	public static final String videoName = "测试 & 回放 名称";
	public static final String subjectName = "测试 * & % ￥活动 名称";
	public static final String callbackurl = "http://t.e.vhall.com/api/callback";

	// demo
	static Callback callback;
	static JLabel fileLabel;
	static JLabel tipsLabel;
	static JProgressBar bar;

	static VhallUploadKit util;
	static File file;
	static String fileKey = "";

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
				startUpload();
			}
		});
		JButton stopBtn = new JButton("停止上传");
		stopBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				stopUpload();
			}
		});
		JButton cancelBtn = new JButton("取消上传");
		cancelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				abortUpload();
			}
		});
		tipsLabel = new JLabel();
		tipsLabel.setText("初始化，请稍等...");
		tipsLabel.setHorizontalAlignment(SwingConstants.CENTER);

		contentPanel.add(fileLabel);
		contentPanel.add(new JLabel());
		contentPanel.add(bar);
		contentPanel.add(selectBtn);
		contentPanel.add(uploadBtn);
		contentPanel.add(stopBtn);
		contentPanel.add(cancelBtn);
		contentPanel.add(new JLabel());
		contentPanel.add(tipsLabel);

		return contentPanel;
	}

	private static void startUpload() {
		if (file == null) {
			tipsLabel.setText("请先选择文件...");
			return;
		}
		String key = util.uploadAndBuildWebinar(file, videoName, subjectName, callback,
				new PutObjectProgressListener(file.length()));
		if (!TextUtils.isEmpty(key))
			fileKey = key;
	}

	private static void stopUpload() {
		if (file == null) {
			tipsLabel.setText("请先选择文件...");
			return;
		}
		if (TextUtils.isEmpty(fileKey)) {
			tipsLabel.setText("请先上传...");
			return;
		}
		if (util.stopUpload(fileKey))
			tipsLabel.setText("上传已停止...");
	}

	private static void abortUpload() {
		if (file == null) {
			tipsLabel.setText("请先选择文件...");
			return;
		}
		if (TextUtils.isEmpty(fileKey)) {
			tipsLabel.setText("请先上传...");
			return;
		}
		if (util.abortUpload(fileKey)){
			tipsLabel.setText("上传已取消...");
			fileKey = "";
		}
			
	}

	/**
	 * 获取上传进度回调
	 */
	static class PutObjectProgressListener implements ProgressListener {

		private long bytesWritten = 0;
		private long totalBytes = -1;
		private boolean succeed = false;
		private long fileLength = 0;

		public PutObjectProgressListener(long fileLength) {
			super();
			this.fileLength = fileLength;
		}

		@Override
		public void progressChanged(ProgressEvent progressEvent) {
			long bytes = progressEvent.getBytes();
			ProgressEventType eventType = progressEvent.getEventType();
			switch (eventType) {
			case TRANSFER_STARTED_EVENT:
				tipsLabel.setText("开始上传...");
				break;
			case REQUEST_CONTENT_LENGTH_EVENT:
				this.totalBytes = bytes;
				this.bytesWritten = fileLength - totalBytes;
				break;
			case REQUEST_BYTE_TRANSFER_EVENT:
				this.bytesWritten += bytes;
				if (this.totalBytes != -1) {
					int percent = (int) (this.bytesWritten * 100.0 / this.fileLength);
					bar.setValue(percent);
					System.out.println(bytes + " bytes have been written at this time, upload progress: " + percent
							+ "%(" + this.bytesWritten + "/" + this.fileLength + ")");
				} else {
					System.out.println(bytes + " bytes have been written at this time, upload ratio: unknown" + "("
							+ this.bytesWritten + "/...)");
				}
				break;

			case TRANSFER_COMPLETED_EVENT:
				this.succeed = true;
				tipsLabel.setText("上传成功!");
				fileKey = "";
				break;

			case TRANSFER_FAILED_EVENT:
				tipsLabel.setText("上传失败!");
				break;

			default:
				break;
			}
		}

		public boolean isSucceed() {
			return succeed;
		}

		@Override
		public void webinarCreate(String fileKey, String webinarId, String recordId) {
//			tipsLabel.setText("文件ID："+fileKey+" 生成回放成功，活动ID："+webinarId+" 片段ID："+recordId);
		}
	}

	private static void selectFile(Component parent) {
		int result = 0;
		JFileChooser fileChooser = new JFileChooser();
		FileSystemView fsv = FileSystemView.getFileSystemView();
		fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
		fileChooser.setDialogTitle("请选择要上传的文件...");
		fileChooser.setApproveButtonText("确定");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		result = fileChooser.showOpenDialog(parent);
		if (JFileChooser.APPROVE_OPTION == result) {
			file = new File(fileChooser.getSelectedFile().getPath());
			fileLabel.setText("待上传文件：" + file.getAbsolutePath());
			// 停止正在上传的文件
			if (!TextUtils.isEmpty(fileKey))
				stopUpload();
			fileKey = "";
		}
	}

}
