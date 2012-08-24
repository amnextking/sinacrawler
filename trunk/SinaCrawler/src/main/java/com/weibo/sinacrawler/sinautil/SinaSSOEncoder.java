package com.weibo.sinacrawler.sinautil;


public class SinaSSOEncoder {
    private boolean i=false;
    private int g=8;
    
    public SinaSSOEncoder(){
        
    }
    //password=sinaSSOEncoder.hex_sha1(""+sinaSSOEncoder.hex_sha1(sinaSSOEncoder.hex_sha1(password))+me.servertime+me.nonce);
    public String encode(String psw,String servertime,String nonce){
        String password;
        password=hex_sha1(""+hex_sha1(hex_sha1(psw))+servertime+nonce);
        return password;
    }
    
    //this.hex_sha1=function(j){return h(b(f(j),j.length*g))};
    private String hex_sha1(String j) {
        return h(b(f(j,j.length()*g), j.length() * g));
    }
    //var h=function(l){
    //var k=i?"0123456789ABCDEF":"0123456789abcdef";
    //var m="";for(var j=0;j<l.length*4;j++)
    //{m+=k.charAt((l[j>>2]>>((3-j%4)*8+4))&15)+k.charAt((l[j>>2]>>((3-j%4)*8))&15)}
    //return m};
    private String h(int[] l){
        String k = i ? "0123456789ABCDEF" : "0123456789abcdef";
        String m = "";
        //System.out.println(l.length*4);
        for (int j = 0; j < l.length * 4; j++) {
            //System.out.println(k.charAt((l[j >> 2] >> ((3 - j % 4) * 8 + 4)) & 15) + k.charAt((l[j >> 2] >> ((3 - j % 4) * 8)) & 15));
            m += k.charAt((l[j >> 2] >> ((3 - j % 4) * 8 + 4)) & 15) + "" + k.charAt((l[j >> 2] >> ((3 - j % 4) * 8)) & 15);
            //System.out.println(m);
        }
        //System.out.println("M:"+m);
        return m;
    }
    
    
//    var b=function(A,r){
//    A[r>>5]|=128<<(24-r%32);A[((r+64>>9)<<4)+15]=r;
//    var B=Array(80);
//    var z=1732584193;
//    var y=-271733879;
//    var v=-1732584194;
//    var u=271733878;
//    var s=-1009589776;
//    for(var o=0;o<A.length;o+=16){
//    var q=z;var p=y;var n=v;var m=u;var k=s;
//    for(var l=0;l<80;l++){
//        if(l<16){B[l]=A[o+l]}
//        else{B[l]=d(B[l-3]^B[l-8]^B[l-14]^B[l-16],1)}
//        var C=e(e(d(z,5),a(l,y,v,u)),e(e(s,B[l]),c(l)));
//        s=u;u=v;v=d(y,30);y=z;z=C}z=e(z,q);
//        y=e(y,p);v=e(v,n);u=e(u,m);s=e(s,k)
//        }
//    return Array(z,y,v,u,s)};
    private int[] b(int[] A,int r){
        //System.out.println("===="+((r+64>>9)<<4)+15);
        //System.out.println(A.length);
        A[r>>5]|=128<<(24-r%32);
        A[((r+64>>9)<<4)+15]=r;
        int[] B = new int[80];
        int z = 1732584193;
        int y = -271733879;
        int v = -1732584194;
        int u = 271733878;
        int s = -1009589776;
        for (int o = 0; o < A.length; o += 16) {
            int q = z;
            int p = y;
            int n = v;
            int m = u;
            int k = s;
            for (int l = 0; l < 80; l++) {
                if (l < 16) {
                    B[l] = A[o + l];
                } else {
                    B[l] = d(B[l - 3] ^ B[l - 8] ^ B[l - 14] ^ B[l - 16], 1);
                }
                int C = e(e(d(z, 5), a(l, y, v, u)), e(e(s, B[l]), c(l)));
                s = u;
                u = v;
                v = d(y, 30);
                y = z;
                z = C;
            }
            z = e(z, q);
            y = e(y, p);
            v = e(v, n);
            u = e(u, m);
            s = e(s, k);
        }
        return new int[]{z,y,v,u,s};
    }
    
    
//    var a=function(k,j,m,l){if(k<20){return(j&m)|((~j)&l)}if(k<40){return j^m^l}if(k<60){return(j&m)|(j&l)|(m&l)}return j^m^l};
    private int a(int k,int j,int m,int l){
        if(k<20){return(j&m)|((~j)&l);};
        if(k<40){return j^m^l;};
        if(k<60){return(j&m)|(j&l)|(m&l);};
        return j^m^l;
    }
//    var c=function(j){return(j<20)?1518500249:(j<40)?1859775393:(j<60)?-1894007588:-899497514};
    private int c(int j){
        return(j<20)?1518500249:(j<40)?1859775393:(j<60)?-1894007588:-899497514;
    }
//    var e=function(j,m){var l=(j&65535)+(m&65535);
//  var k=(j>>16)+(m>>16)+(l>>16);return(k<<16)|(l&65535)};
    private int e(int j, int m) {
        int l = (j & 65535) + (m & 65535);
        int k = (j >> 16) + (m >> 16) + (l >> 16);
        return (k << 16) | (l & 65535);
    }
//    var d=function(j,k){return(j<<k)|(j>>>(32-k))};
    private int d(int j,int k){
        return(j<<k)|(j>>>(32-k));
    }
    
    
    //var f=function(m){
    //var l=Array();var j=(1<<g)-1;
    //for(var k=0;k<m.length*g;k+=g){
    //l[k>>5]|=(m.charCodeAt(k/g)&j)<<(24-k%32)
    //}return l};
    
    private int[] f(String m,int r){
        int[] l;
        int j = (1<<this.g)-1;
        int len=((r+64>>9)<<4)+15;
        int k;
        for(k=0;k<m.length()*g;k+=g){
            //System.out.println(k>>5);
            len = k>>5>len?k>>5:len;
        }
        l = new int[len+1];
        //System.out.println("++++++++"+l.length);
        for(k=0;k<l.length;k++){
            l[k]=0;
        }
        for(k=0;k<m.length()*g;k+=g){
            l[k>>5]|=(m.charAt(k/g)&j)<<(24-k%32);
        }
        return l;
    }
}
