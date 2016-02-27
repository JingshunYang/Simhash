/**
 * Function: simHash 判断文本相似度，该示例程支持中文<br/>
 * date: 2013-8-6 上午1:11:48 <br/>
 * @author june
 * @version 0.1
 */
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SimHash {

    private String tokens;
    private BigInteger intSimHash;
    private String strSimHash;
    private int hashbits = 64;

    public SimHash(String tokens) throws IOException {
        this.tokens = tokens;
        this.intSimHash = this.simHash();
    }

    public SimHash(String tokens, int hashbits) throws IOException {
        this.tokens = tokens;
        this.hashbits = hashbits;
        this.intSimHash = this.simHash();
    }

    HashMap<String, Integer> wordMap = new HashMap<String, Integer>();

    public BigInteger simHash() throws IOException {
        // 定义特征向量/数组
        int[] v = new int[this.hashbits];
        String[] sim  = tokens.split("#");

        System.out.print("关键字：");

        for(int  i= 0; i< sim.length; i++){
            String[] word = sim[i].split("/");
            String w = word[0];
            System.out.print(w+ " ");
            BigInteger t = this.hash(w);
            for (int j = 0; j < this.hashbits; j++) {
                BigInteger bitmask = new BigInteger("1").shiftLeft(j);
                if (t.and(bitmask).signum() != 0) {
                    v[j] += 1;
                } else {
                    v[j] -= 1;
                }
            }
        }

        System.out.println();
        BigInteger fingerprint = new BigInteger("0");
        StringBuffer simHashBuffer = new StringBuffer();
        for (int i = 0; i < this.hashbits; i++) {
            // 4、最后对数组进行判断,大于0的记为1,小于等于0的记为0,得到一个 64bit 的数字指纹/签名.
            if (v[i] >= 0) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
                simHashBuffer.append("1");
            } else {
                simHashBuffer.append("0");
            }
        }
        this.strSimHash = simHashBuffer.toString();
        System.out.println(this.strSimHash + " length " + this.strSimHash.length());
        return fingerprint;
    }

    private BigInteger hash(String source) {
        if (source == null || source.length() == 0) {
            return new BigInteger("0");
        } else {
            char[] sourceArray = source.toCharArray();
            BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
            BigInteger m = new BigInteger("1000003");
            BigInteger mask = new BigInteger("2").pow(this.hashbits).subtract(new BigInteger("1"));
            for (char item : sourceArray) {
                BigInteger temp = BigInteger.valueOf((long) item);
                x = x.multiply(m).xor(temp).and(mask);
            }
            x = x.xor(new BigInteger(String.valueOf(source.length())));
            if (x.equals(new BigInteger("-1"))) {
                x = new BigInteger("-2");
            }
            return x;
        }
    }

    public int hammingDistance(SimHash other) {

        BigInteger x = this.intSimHash.xor(other.intSimHash);
        int tot = 0;

        // 统计x中二进制位数为1的个数
        // 我们想想，一个二进制数减去1，那么，从最后那个1（包括那个1）后面的数字全都反了，
        // 对吧，然后，n&(n-1)就相当于把后面的数字清0，
        // 我们看n能做多少次这样的操作就OK了。

        while (x.signum() != 0) {
            tot += 1;
            x = x.and(x.subtract(new BigInteger("1")));
        }
        return tot;
    }

    public int getDistance(String str1, String str2) {
        int distance;
        if (str1.length() != str2.length()) {
            distance = -1;
        } else {
            distance = 0;
            for (int i = 0; i < str1.length(); i++) {
                if (str1.charAt(i) != str2.charAt(i)) {
                    distance++;
                }
            }
        }
        return distance;
    }

    public List subByDistance(SimHash simHash, int distance) {
        // 分成几组来检查
        int numEach = this.hashbits / (distance + 1);
        List characters = new ArrayList();

        StringBuffer buffer = new StringBuffer();

        int k = 0;
        for (int i = 0; i < this.intSimHash.bitLength(); i++) {
            // 当且仅当设置了指定的位时，返回 true
            boolean sr = simHash.intSimHash.testBit(i);

            if (sr) {
                buffer.append("1");
            } else {
                buffer.append("0");
            }

            if ((i + 1) % numEach == 0) {
                // 将二进制转为BigInteger
                BigInteger eachValue = new BigInteger(buffer.toString(), 2);
                System.out.println("----" + eachValue);
                buffer.delete(0, buffer.length());
                characters.add(eachValue);
            }
        }

        return characters;
    }

    public static void main(String[] args) throws Exception {
        Connection con = JDBCUtil.getConnection();
       Statement stmt = con.createStatement();//tatement接口需要通过connection接口进行实例化操作
       ResultSet  result = stmt.executeQuery("select *  from exam_question");//执行sql语句，结果集放在result中
        SimHash[] hash = new SimHash[50];
        for (int i = 0; result.next();i++){
            System.out.println("\nNo."+(i+1)+"=====================================================");
            String question = result.getString("question");//获取数据库person表中name字段的值
            System.out.println(question);
            String words = result.getString("key_words");
            hash[i] = new SimHash(words,64);
            System.out.println(hash[i].intSimHash+" "+hash[i].intSimHash.bitLength());

        }

        System.out.println("============================");

        for(int i = 0; i<50; i++){
            int dis = hash[35].getDistance(hash[35].strSimHash, hash[i].strSimHash);
            System.out.println("No." + (i+1) +"\tDis:"+ hash[35].hammingDistance(hash[i]));
        }

        // 根据鸽巢原理（也成抽屉原理，见组合数学），如果两个签名的海明距离在 3 以内，它们必有一块签名subByDistance()完全相同。

        JDBCUtil.close(result);
        JDBCUtil.close(con);
    }
}