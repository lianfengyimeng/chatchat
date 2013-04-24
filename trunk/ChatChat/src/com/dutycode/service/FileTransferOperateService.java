package com.dutycode.service;

import java.io.File;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

/**
 * 文件传输
 * 
 * @author michael
 * 
 */
public class FileTransferOperateService {

	private XMPPConnection conncetion = (XMPPConnection) ClientConServer.connection;

	public FileTransferOperateService() {

	}

	/**
	 * 发送文件
	 * 
	 * @param _userJID
	 * @param _filePath
	 *            文件路径
	 * @param _fileDescription
	 *            文件描述
	 */
	public OutgoingFileTransfer sendFile(String _userJID, String _filePath,
			String _fileDescription) {

		/*
		 * 下面这段内容是必须的但是我不清楚为什么要这么做
		 * */
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(conncetion);

		if (sdm == null)
			sdm = new ServiceDiscoveryManager(conncetion);

		sdm.addFeature("http://jabber.org/protocol/disco#info");
		sdm.addFeature("jabber:iq:privacy");

		// Create the file transfer manager
		FileTransferManager manager = new FileTransferManager(conncetion);
		FileTransferNegotiator.setServiceEnabled(conncetion, true);

		String fileTo = conncetion.getRoster().getPresence(_userJID).getFrom();
		OutgoingFileTransfer outfiletransfer = manager
				.createOutgoingFileTransfer(fileTo);

		try {
			outfiletransfer.sendFile(new File(_filePath), _fileDescription);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		
		return outfiletransfer;
	}
}
