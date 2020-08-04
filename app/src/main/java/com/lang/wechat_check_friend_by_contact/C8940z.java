package com.lang.wechat_check_friend_by_contact;

import java.nio.ByteBuffer;

/* renamed from: com.tencent.mm.sdk.platformtools.z */
public final class C8940z {
    private boolean Grj;
    private ByteBuffer byteBuffer;

    /* renamed from: cq */
    private static int m13040cq(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return -1;
        }
        if (bArr[0] != 123) {
            return -2;
        }
        if (bArr[bArr.length - 1] != 125) {
            return -3;
        }
        return 0;
    }

    /* renamed from: cr */
    public final int mo14099cr(byte[] bArr) {
        this.byteBuffer = ByteBuffer.wrap(bArr);
        this.byteBuffer.position(1);
        this.Grj = false;
        return 0;
    }

    public final int getInt() {
        int i = this.byteBuffer.getInt();
        return i;
    }

    public final long getLong() {
        long j = this.byteBuffer.getLong();
        return j;
    }

    public final byte[] getBuffer() throws Exception {
        int i = this.byteBuffer.getShort();
        if (i > 3072) {
            this.byteBuffer = null;
            Exception exc2 = new Exception("Buffer String Length Error");
            throw exc2;
        } else if (i == 0) {
            byte[] bArr = new byte[0];
            return bArr;
        } else {
            byte[] bArr2 = new byte[i];
            this.byteBuffer.get(bArr2, 0, i);
            return bArr2;
        }
    }

    public final String getString() throws Exception {
        int i = this.byteBuffer.getShort();
        if (i > 3072) {
            this.byteBuffer = null;
            Exception exc2 = new Exception("Buffer String Length Error");
            throw exc2;
        } else if (i == 0) {
            return "";
        } else {
            byte[] bArr = new byte[i];
            this.byteBuffer.get(bArr, 0, i);
            String str = new String(bArr, "UTF-8");
            return str;
        }
    }

    /* renamed from: Yp */
    public final void mo14096Yp(int i) {
        ByteBuffer byteBuffer2 = this.byteBuffer;
        byteBuffer2.position(byteBuffer2.position() + i);
    }

    public final void eUD() throws Exception {
        short s = this.byteBuffer.getShort();
        if (s > 3072) {
            this.byteBuffer = null;
            Exception exc2 = new Exception("Buffer String Length Error");
            throw exc2;
        } else if (s == 0) {

        } else {
            this.byteBuffer.position(s + this.byteBuffer.position());
        }
    }

    public final boolean eUE() {
        if (this.byteBuffer.limit() - this.byteBuffer.position() <= 1) {
            return true;
        }
        return false;
    }

    public final int eUF() {
        this.byteBuffer = ByteBuffer.allocate(4096);
        this.byteBuffer.put((byte) 123);
        this.Grj = true;
        return 0;
    }

    /* renamed from: Yq */
    private int m13039Yq(int i) {
        if (this.byteBuffer.limit() - this.byteBuffer.position() > i) {
        } else {
            ByteBuffer allocate = ByteBuffer.allocate(this.byteBuffer.limit() + 4096);
            allocate.put(this.byteBuffer.array(), 0, this.byteBuffer.position());
            this.byteBuffer = allocate;
        }
        return 0;
    }

    /* renamed from: Yr */
    public final int mo14097Yr(int i) {
        m13039Yq(4);
        this.byteBuffer.putInt(i);
        return 0;
    }

    /* renamed from: Af */
    public final int mo14095Af(long j) {
        m13039Yq(8);
        this.byteBuffer.putLong(j);
        return 0;
    }

    /* renamed from: cs */
    public final int mo14100cs(byte[] bArr) {
        byte[] bArr2 = null;
        if (bArr != null) {
            bArr2 = bArr;
        }
        if (bArr2 == null) {
            bArr2 = new byte[0];
        }
        m13039Yq(bArr2.length + 2);
        this.byteBuffer.putShort((short) bArr2.length);
        if (bArr2.length > 0) {
            this.byteBuffer.put(bArr2);
        }
        return 0;
    }

    public final int aKJ(String str) throws Exception {
        byte[] bArr = null;
        if (str != null) {
            bArr = str.getBytes();
        }
        if (bArr == null) {
            bArr = new byte[0];
        }
        if (bArr.length > 3072) {
            throw new Exception("Buffer String Length Error");
        }
        m13039Yq(bArr.length + 2);
        this.byteBuffer.putShort((short) bArr.length);
        if (bArr.length > 0) {
            this.byteBuffer.put(bArr);
        }
        return 0;
    }

    public final byte[] eUG() {
        m13039Yq(1);
        this.byteBuffer.put((byte) 125);
        byte[] bArr = new byte[this.byteBuffer.position()];
        System.arraycopy(this.byteBuffer.array(), 0, bArr, 0, bArr.length);
        return bArr;
    }
}