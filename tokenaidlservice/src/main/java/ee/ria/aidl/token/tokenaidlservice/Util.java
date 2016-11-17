package ee.ria.aidl.token.tokenaidlservice;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Util {
	static byte[] concat(byte[]...arrays) {
		int size = 0;
		for (byte[] array: arrays) {
			size += array.length;
		}
		byte[] result = new byte[size];
		int pos = 0;
		for (byte[] array: arrays) {
			System.arraycopy(array, 0, result, pos, array.length);
			pos += array.length;
		}
		return result;
	}

	static String toHex(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
	}

	static byte[] fromHex(String hexData) {
		int len = hexData.length();
		byte[] data = new byte[len/2];
		for(int i = 0; i < len; i+=2){
			data[i/2] = (byte) ((Character.digit(hexData.charAt(i), 16) << 4) + Character.digit(hexData.charAt(i+1), 16));
		}
		return data;
	}

	static X509Certificate getX509Certificate(byte[] certificate) throws CertificateException {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		return (X509Certificate) certFactory.generateCertificate(
				new ByteArrayInputStream(certificate));
	}

	public static String getCommonName(byte[] certificate) {
		try {
			X509Certificate cert = getX509Certificate(certificate);
			cert.getVersion();
			for (String x : cert.getSubjectDN().getName().replace("\\,", " ").split(",")) {
				if (x.contains("CN=")) {
					return x.replace("CN=", "").trim();
				}
			}
			return cert.getSubjectDN().getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}