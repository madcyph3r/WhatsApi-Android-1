package nl.giovanniterlingen.whatsapp;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class KeyStream {

	private final RC4 rc4;
	private final byte[] key;
	private final byte[] macKey;
	public int seq = 0;

	public KeyStream(byte[] key, byte[] macKey) {
		rc4 = new RC4(key, 768);
		this.key = key;
		this.macKey = macKey;
	}

	public byte[] encode(byte[] data, int macOffset, int offset, int length)
			throws EncodeException {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			byte[] d = rc4.cipher(data, offset, length);
			byte[] hash_hmac = computeMac(d, offset, length);
			if (macOffset > 0)
				stream.write(Arrays.copyOfRange(d, 0, macOffset));
			stream.write(Arrays.copyOfRange(hash_hmac, 0, 4));
			stream.write(Arrays.copyOfRange(d, macOffset, data.length));
			return stream.toByteArray();
		} catch (Exception e) {
			throw new EncodeException(e);
		}
	}

	public static byte[] hash_hmac(byte[] d, byte[] mykey)
			throws NoSuchAlgorithmException, InvalidKeyException {
		Mac mac = Mac.getInstance("HmacSHA1");
		SecretKeySpec secret = new SecretKeySpec(mykey, "HmacSHA1");
		mac.init(secret);
		byte[] digest = mac.doFinal(d);
		return digest;
	}

	public byte[] decode(byte[] data, int macOffset, int offset, int length)
			throws DecodeException {
		byte[] mac;
		try {
			mac = computeMac(data, offset, length);
		} catch (Exception e) {
			throw new DecodeException(e);
		}
		// validate mac
		for (int i = 0; i < 4; i++) {
			int foo = data[macOffset + i];
			int bar = mac[i];
			if (foo != bar) {
				throw new DecodeException("MAC mismatch: " + foo + " != " + bar);
			}
		}
		return rc4.cipher(data, offset, length);
	}

	byte[] computeMac(byte[] data, int offset, int length)
			throws InvalidKeyException, NoSuchAlgorithmException {
		// log.debug("Mac sequence: "+seq);
		Mac mac = Mac.getInstance("HmacSHA1");
		SecretKeySpec secret = new SecretKeySpec(macKey, "HmacSHA1");
		mac.init(secret);
		byte[] d = Arrays.copyOfRange(data, offset, offset + length);
		mac.update(d);

		byte[] array = new byte[] { (byte) (seq >> 24), (byte) (seq >> 16),
				(byte) (seq >> 8), (byte) (seq) };
		seq++;

		byte[] digest = mac.doFinal(array);
		return digest;
	}

	class RC4 {

		private int i;
		private int j;
		private byte[] s;

		public RC4(byte[] key, int drop) {
			s = range(0, 255);
			for (int i1 = 0, j1 = 0; i1 < 256; ++i1) {
				int k = key[i1 % key.length] & 255;
				j1 = (j1 + k + (s[i1] & 255)) & 255;
				swap(i1, j1);
			}
			i = 0;
			j = 0;
			cipher(range(0, drop), 0, drop);
		}

		private byte[] range(int k, int l) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			for (int ctr = k; ctr <= l; ++ctr) {
				stream.write(ctr);
			}
			return stream.toByteArray();
		}

		public byte[] cipher(byte[] data, int offset, int length) {
			ByteArrayOutputStream ret = new ByteArrayOutputStream();
			for (int n = length; n > 0; n--) {
				i = (i + 1) & 255;
				j = (j + s[i]) & 255;
				swap(i, j);
				byte d = data[offset++];
				ret.write((d ^ s[(s[i] + s[j]) & 255]));
			}
			for (int n = length; n < data.length; ++n) {
				ret.write(data[n]);
			}
			return ret.toByteArray();
		}

		private void swap(int i2, int j2) {
			byte c = s[i2];
			s[i2] = s[j2];
			s[j2] = c;
		}
	}
}
