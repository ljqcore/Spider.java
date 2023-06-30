package spider;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;
import static java.lang.System.getProperty;

import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class spider extends JFrame implements ActionListener {

    private String textType = "UTF-8";
    // key:sensword  value:level
    private HashMap<String, String> wordMap = new HashMap<String, String>();
    // 保存对应敏感词的出现次数
    private ArrayList<Integer> wordNum = new ArrayList<Integer>();
    // 存储超链接列表
    private ArrayList<String> hreflist = new ArrayList<String>();
    private MySqlConnection sqlConn = new MySqlConnection();

    //开始界面
    private JLabel title_jlb = null;
    private JButton jbt_begin = new JButton("开始检测");
    private JButton jbt_saveData = new JButton("导出历史检测数据到本地");
    private JButton jbt_exit = new JButton("退出");

    //单个网址检测界面
    private JLabel site_jlb = null;
    private JTextField site_jtf = new JTextField(25);
    private JButton goSingleSpider_jbt = null;

    //多个网址检测界面
    private JLabel siteFile_jlb = null;
    private JTextField siteFile_jtf = new JTextField(25);
    private JButton siteFile_jbt = null;
    private ArrayList<String> siteList = new ArrayList<String>();
    private JButton goSeveralSpider_jbt = null;

    //单、多网址共同组件
    JFrame jf = new JFrame();
    private JLabel charset_jlb = null;
    private JComboBox<String> charset = new JComboBox<String>();
    private JLabel sensWord_jlb = null;
    private JTextField sensWord_jtf = new JTextField(25);
    private JButton sensWord_jbt = null;

    //结果界面
    JFrame jf_result = new JFrame();

    public spider() {
        //设置界面风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
        }

        this.setTitle("敏感词检测器");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setBounds(400, 200, 700, 500);
        JPanel jpl1 = new JPanel();
        jpl1.setLayout(null);
        this.add(jpl1);

        title_jlb = new JLabel("敏感词语检测器");
        title_jlb.setBounds(190, 100, 300, 50);
        title_jlb.setFont(new Font("宋体", Font.BOLD, 40));
        jpl1.add(title_jlb);


        jbt_begin.setBounds(200, 250, 280, 40);
        jbt_begin.setFont(new Font("宋体", Font.BOLD, 20));
        jpl1.add(jbt_begin);
        jbt_begin.addActionListener(this);

        jbt_saveData.setBounds(200, 300, 280, 40);
        jbt_saveData.setFont(new Font("宋体", Font.BOLD, 20));
        jpl1.add(jbt_saveData);
        jbt_saveData.addActionListener(this);

        jbt_exit.setBounds(200, 350, 280, 40);
        jbt_exit.setFont(new Font("宋体", Font.BOLD, 20));
        jpl1.add(jbt_exit);
        jbt_exit.addActionListener(this);

        this.setVisible(true);


        charset_jlb = new JLabel("编码格式:");
        charset_jlb.setBounds(70, 150, 100, 30);
        charset_jlb.setFont(new Font("宋体", Font.BOLD, 20));
        //下拉框--编码格式
        charset.addItem("UTF-8");
        charset.addItem("GBK");
        charset.setEditable(false);
        charset.setBounds(190, 150, 350, 30);
        charset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textType = (String) charset.getSelectedItem();
            }
        });

        sensWord_jlb = new JLabel("敏感词库:");
        sensWord_jlb.setBounds(70, 50, 100, 30);
        sensWord_jlb.setFont(new Font("宋体", Font.BOLD, 20));
        //文本框"词库文件"
        sensWord_jtf.setBounds(190, 50, 350, 30);
        sensWord_jtf.setEditable(false);

        sensWord_jbt = new JButton("选择敏感词文件");
        sensWord_jbt.setBounds(190, 100, 350, 30);
        sensWord_jbt.addActionListener(this);
        sensWord_jbt.setFont(new Font("宋体", Font.BOLD, 18));

        goSingleSpider_jbt = new JButton("爬取并分析敏感词");
        goSingleSpider_jbt.setBounds(230, 370, 200, 30);
        goSingleSpider_jbt.setFont(new Font("宋体", Font.BOLD, 20));
        goSingleSpider_jbt.addActionListener(this);

        siteFile_jbt = new JButton("选择网址文件");
        siteFile_jbt.setBounds(190, 250, 350, 30);
        siteFile_jbt.addActionListener(this);
        siteFile_jbt.setFont(new Font("宋体", Font.BOLD, 18));

        goSeveralSpider_jbt = new JButton("爬取并分析敏感词");
        goSeveralSpider_jbt.setBounds(230, 370, 200, 30);
        goSeveralSpider_jbt.setFont(new Font("宋体", Font.BOLD, 20));
        goSeveralSpider_jbt.addActionListener(this);
    }

    public void singlesiteUI() {

        site_jlb = new JLabel("输入网址:");
        site_jlb.setBounds(70, 200, 100, 30);
        site_jlb.setFont(new Font("宋体", Font.BOLD, 20));

        //文本框--网址
        site_jtf.setBounds(190, 200, 350, 30);

        //文本框清空
        site_jtf.setText("");
        sensWord_jtf.setText("");

        JFrame jf_temp = new JFrame();//一直用jf的话界面无法重绘
        jf = jf_temp;
        jf_temp.setTitle("Spider_single_site");
        jf_temp.setDefaultCloseOperation(2);
        jf_temp.setBounds(400, 200, 700, 500);
        JPanel jpl = new JPanel();
        jpl.setLayout(null);
        jf_temp.add(jpl);

        jpl.add(site_jlb);
        jpl.add(site_jtf);
        jpl.add(charset_jlb);
        jpl.add(charset);
        jpl.add(sensWord_jlb);
        jpl.add(sensWord_jtf);
        jpl.add(sensWord_jbt);
        jpl.add(goSingleSpider_jbt);

        jf_temp.setVisible(true);
    }

    public void severalsiteUI() {

        siteFile_jlb = new JLabel("网址文件:");
        siteFile_jlb.setBounds(70, 200, 100, 30);
        siteFile_jlb.setFont(new Font("宋体", Font.BOLD, 20));

        //文本框--网址文件名
        siteFile_jtf.setBounds(190, 200, 350, 30);
        siteFile_jtf.setEditable(false);

        //文本框清空
        siteFile_jtf.setText("");
        sensWord_jtf.setText("");

        JFrame jf_temp = new JFrame();//一直用jf的话界面无法重绘
        jf = jf_temp;
        jf_temp.setTitle("Spider_several_sites");
        jf_temp.setDefaultCloseOperation(2);
        jf_temp.setBounds(400, 200, 700, 500);
        JPanel jpl = new JPanel();
        jpl.setLayout(null);
        jf_temp.add(jpl);

        jpl.add(siteFile_jlb);
        jpl.add(siteFile_jtf);
        jpl.add(siteFile_jbt);
        jpl.add(charset_jlb);
        jpl.add(charset);
        jpl.add(sensWord_jlb);
        jpl.add(sensWord_jtf);
        jpl.add(sensWord_jbt);
        jpl.add(goSeveralSpider_jbt);

        jf_temp.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        new spider();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton j = (JButton) e.getSource();
        if (j == jbt_begin) {
            Object[] kind = {"单个网址", "多个网址"};
            int choice = JOptionPane.showOptionDialog(this, "请选择网址类型", null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, kind, kind[0]);
            if (choice == 0) {
                singlesiteUI();
            } else if (choice == 1) {
                severalsiteUI();
            }
        } else if (j == jbt_saveData) {  // 保存到csv文件
            try {
                sqlConn.ExportToCSV();
                JOptionPane.showMessageDialog(this, "数据成功导出到CSV文件。", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "数据导出失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else if (j == jbt_exit) {
            exit(0);
        } else if (j == sensWord_jbt) {//选择词库文件按钮
            getLib();
        } else if (j == goSingleSpider_jbt) {
            //隐藏原界面，弹出新界面(边界布局)
            jf.setVisible(false);

            JFrame jf_result_temp = new JFrame();
            jf_result = jf_result_temp;
            jf_result_temp.setTitle("爬取结果展示");
            jf_result_temp.setDefaultCloseOperation(2);
            jf_result_temp.setBounds(400, 200, 700, 500);
            jf_result_temp.setLayout(new BorderLayout());

            JPanel jpl_result = new JPanel();
            JPanel jpl_lib = new JPanel();
            jf_result_temp.add(jpl_result, BorderLayout.WEST);
            jf_result_temp.add(jpl_lib, BorderLayout.CENTER);
            jpl_result.setPreferredSize(new Dimension(450, 0));
            jpl_result.setBackground(new Color(220, 220, 100));
            jpl_lib.setBackground(new Color(100, 180, 100));

            //左边的面板
            JPanel jpl_left1 = new JPanel();
            jpl_result.add(jpl_left1);
            JTextArea result_jta = new JTextArea(23, 53);
            JScrollPane result_jsp = new JScrollPane(result_jta);
            result_jta.setLineWrap(true);
            result_jta.setEditable(false);
            jpl_left1.add(result_jsp, BorderLayout.CENTER);
            JTabbedPane tabPane1 = new JTabbedPane();
            tabPane1.add("网页文本", jpl_left1);
            jpl_result.add(tabPane1, BorderLayout.CENTER);

            //右边的面板
            JPanel jpl_right1 = new JPanel();
            JPanel jpl_right2 = new JPanel();
            jpl_lib.add(jpl_right1);
            jpl_lib.add(jpl_right2);
            JTextArea sensWord_jta = new JTextArea(23, 28);
            JScrollPane sensWord_jsp = new JScrollPane(sensWord_jta);
            JTextArea sensNum_jta = new JTextArea(23, 28);
            JScrollPane sensNum_jsp = new JScrollPane(sensNum_jta);
            sensWord_jta.setEditable(false);
            sensNum_jta.setEditable(false);
            jpl_right1.add(sensWord_jsp);
            jpl_right2.add(sensNum_jsp);
            JTabbedPane tabPane2 = new JTabbedPane();
            tabPane2.add("敏感词库", jpl_right1);
            tabPane2.add("统计结果", jpl_right2);
            jpl_lib.add(tabPane2, BorderLayout.CENTER);

            // URL爬取网页HTML代码文本
            String website = site_jtf.getText();
            String text_html = getHtml(website);
            // 将HTML代码文本转换成文本
            hreflist.clear();
            String text_str = getText(text_html);
            // 将文本内容加到左边面板上
            result_jta.append(text_str);

            // 高亮检测
            showSensword(result_jta);

            // 将该次爬取的网页中的超链接保存到csv文件
            try {
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("href.csv"), StandardCharsets.UTF_8);
                // 写入
                for (int i = 0; i < hreflist.size(); i++) {
                    writer.append(hreflist.get(i));
                    writer.append("\n");
                }
                writer.flush();
            } catch (Exception ex) {
            }


            // 获取当前时间并格式化成mysql的datetime
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formatDateTime = now.format(formatter);

            // 将敏感词检测结果储存到数据库
            for (int i = 0; i < wordMap.size(); i++) {
                try {
                    String key = (String) wordMap.keySet().toArray()[i];
                    sqlConn.insert(website, formatDateTime, key, wordNum.get(i), wordMap.get(key));
                } catch (Exception ex) {
                }
            }

            // 将敏感词库放到右边面板第一个文本框
            sensWord_jta.append("高风险词：" + "\n");
            for (Map.Entry<String, String> entry : wordMap.entrySet()) {
                if ("高风险词".equals(entry.getValue())) {
                    sensWord_jta.append(entry.getKey() + "\n");
                }
            }
            sensWord_jta.append("\n" + "中风险词：" + "\n");
            for (Map.Entry<String, String> entry : wordMap.entrySet()) {
                if ("中风险词".equals(entry.getValue())) {
                    sensWord_jta.append(entry.getKey() + "\n");
                }
            }
            sensWord_jta.append("\n" + "低风险词：" + "\n");
            for (Map.Entry<String, String> entry : wordMap.entrySet()) {
                if ("低风险词".equals(entry.getValue())) {
                    sensWord_jta.append(entry.getKey() + "\n");
                }
            }


            // 将敏感词统计次数放到右边面板第二个文本框
            sensNum_jta.append("高风险词：" + "\n");
            for (int i = 0; i < wordMap.size(); i++) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) wordMap.entrySet().toArray()[i];
                if ("高风险词".equals(entry.getValue()) && wordNum.get(i) != 0) {
                    sensNum_jta.append(entry.getKey() + ": " + wordNum.get(i) + "处" + "\n");
                }
            }
            sensNum_jta.append("\n" + "中风险词：" + "\n");
            for (int i = 0; i < wordMap.size(); i++) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) wordMap.entrySet().toArray()[i];
                if ("中风险词".equals(entry.getValue()) && wordNum.get(i) != 0) {
                    sensNum_jta.append(entry.getKey() + ": " + wordNum.get(i) + "处" + "\n");
                }
            }
            sensNum_jta.append("\n" + "低风险词：" + "\n");
            for (int i = 0; i < wordMap.size(); i++) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) wordMap.entrySet().toArray()[i];
                if ("低风险词".equals(entry.getValue()) && wordNum.get(i) != 0) {
                    sensNum_jta.append(entry.getKey() + ": " + wordNum.get(i) + "处" + "\n");
                }
            }

            jf_result_temp.setVisible(true);

        } else if (j == siteFile_jbt) {
            //选择网址文件
            JFileChooser fChooser = new JFileChooser();
            int ok = fChooser.showOpenDialog(this);
            if (ok != JFileChooser.APPROVE_OPTION) return;
            File file = fChooser.getSelectedFile();

            //将文件名显示在标签上
            siteFile_jtf.setText(fChooser.getSelectedFile().getPath());

            //读取文件，获取每个文件名,存入列表
            siteList.clear();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                while (true) {
                    String str = br.readLine();
                    if (str == null) break;
                    siteList.add(str);
                }
                br.close();
            } catch (FileNotFoundException e1) {
                JOptionPane.showMessageDialog(null, "文件不存在");
                e1.printStackTrace();
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(null, "文件读取失败");
                e1.printStackTrace();
            }

        } else if (j == goSeveralSpider_jbt) {
            //隐藏原界面，弹出新界面(边界布局)
            jf.setVisible(false);

            JFrame jf_result_temp = new JFrame();
            jf_result = jf_result_temp;
            jf_result_temp.setTitle("Spider_result");
            jf_result_temp.setDefaultCloseOperation(2);
            jf_result_temp.setBounds(400, 200, 700, 500);
            jf_result_temp.setLayout(new BorderLayout());

            //左右的一级面板--jpl_result和jpl_lib
            JPanel jpl_result = new JPanel();
            JPanel jpl_lib = new JPanel();
            jf_result_temp.add(jpl_result, BorderLayout.WEST);
            jf_result_temp.add(jpl_lib, BorderLayout.CENTER);
            jpl_result.setPreferredSize(new Dimension(450, 0));
            jpl_result.setBackground(new Color(220, 220, 100));
            jpl_lib.setBackground(new Color(100, 180, 100));

            //左边的二级面板--根据网址数生成对应数量的面板
            int num = siteList.size();
            JPanel[] jpl_lefts = new JPanel[10];//预设最大10个面板
            JTextArea[] jta_lefts = new JTextArea[10];
            JScrollPane[] jsp_lefts = new JScrollPane[10];
            for (int i = 0; i < 10; i++) {//全部初始化
                jpl_lefts[i] = new JPanel();
                jta_lefts[i] = new JTextArea(23, 53);
                jsp_lefts[i] = new JScrollPane(jta_lefts[i]);
                jta_lefts[i].setLineWrap(true);
                jta_lefts[i].setEditable(false);
                jpl_lefts[i].add(jsp_lefts[i], BorderLayout.CENTER);
            }
            JTabbedPane tabPane1 = new JTabbedPane();
            for (int i = 0; i < num; i++) {   // 每个网址配一个面板，一个文本框，一个滚动条
                tabPane1.add("网页" + (i + 1) + "文本", jpl_lefts[i]);
            }
            jpl_result.add(tabPane1, BorderLayout.CENTER);

            //右边的二级面板
            JPanel jpl_right1 = new JPanel();
            JPanel jpl_right2 = new JPanel();
            jpl_lib.add(jpl_right1);
            jpl_lib.add(jpl_right2);
            JTextArea sensWord_jta = new JTextArea(23, 28);
            JScrollPane sensWord_jsp = new JScrollPane(sensWord_jta);
            JTextArea sensNum_jta = new JTextArea(23, 28);
            JScrollPane sensNum_jsp = new JScrollPane(sensNum_jta);
            sensWord_jta.setEditable(false);
            sensWord_jta.setLineWrap(true);
            sensNum_jta.setEditable(false);
            sensNum_jta.setLineWrap(true);
            jpl_right1.add(sensWord_jsp);
            jpl_right2.add(sensNum_jsp);
            JTabbedPane tabPane2 = new JTabbedPane();
            tabPane2.add("敏感词库", jpl_right1);
            tabPane2.add("统计结果", jpl_right2);
            jpl_lib.add(tabPane2, BorderLayout.CENTER);


            hreflist.clear();
            // 爬取多个网页并解析
            for (int i = 0; i < siteList.size(); i++) {
                // URL爬取网页HTML代码文本
                String website = siteList.get(i);
                String text_html = getHtml(website);

                // 将HTML代码文本转换成文本
                String text_str = getText(text_html);

                // 将文本内容加到左边面板上
                jta_lefts[i].append(text_str);

                // 获取当前时间
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formatDateTime = now.format(formatter);

                ArrayList<Integer> num_old = new ArrayList<Integer>();
                num_old.addAll(wordNum);

//                System.out.println(num_old);
                // 统计敏感词库
                showSensword(jta_lefts[i]);
//                System.out.println(wordNum);

                // 将敏感词库存入数据库
                for (int k = 0; k < wordMap.size(); k++) {
                    try {
                        String key = (String) wordMap.keySet().toArray()[k];
                        sqlConn.insert(website, formatDateTime, key, wordNum.get(k) - num_old.get(k), wordMap.get(key));
                    } catch (Exception ex) {
                    }
                }
            }

            // 将超链接存入文本文件中
            try {
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("href.csv"), StandardCharsets.UTF_8);
                // 写入
                for (int i = 0; i < hreflist.size(); i++) {
                    writer.append(hreflist.get(i));
                    writer.append("\n");
                }
                writer.flush();
            } catch (Exception ex) {
            }

            //将敏感词库放到右边面板第一个文本框
            sensWord_jta.append("高风险词：" + "\n");
            for (Map.Entry<String, String> entry : wordMap.entrySet()) {
                if ("高风险词".equals(entry.getValue())) {
                    sensWord_jta.append(entry.getKey() + "\n");
                }
            }
            sensWord_jta.append("\n" + "中风险词：" + "\n");
            for (Map.Entry<String, String> entry : wordMap.entrySet()) {
                if ("中风险词".equals(entry.getValue())) {
                    sensWord_jta.append(entry.getKey() + "\n");
                }
            }
            sensWord_jta.append("\n" + "低风险词：" + "\n");
            for (Map.Entry<String, String> entry : wordMap.entrySet()) {
                if ("低风险词".equals(entry.getValue())) {
                    sensWord_jta.append(entry.getKey() + "\n");
                }
            }

            //将敏感词统计次数放到右边面板第二个文本框
            sensNum_jta.append("高风险词：" + "\n");
            for (int i = 0; i < wordMap.size(); i++) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) wordMap.entrySet().toArray()[i];
                if ("高风险词".equals(entry.getValue()) && wordNum.get(i) != 0) {
                    sensNum_jta.append(entry.getKey() + ": " + wordNum.get(i) + "处" + "\n");
                }
            }
            sensNum_jta.append("\n" + "中风险词：" + "\n");
            for (int i = 0; i < wordMap.size(); i++) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) wordMap.entrySet().toArray()[i];
                if ("中风险词".equals(entry.getValue()) && wordNum.get(i) != 0) {
                    sensNum_jta.append(entry.getKey() + ": " + wordNum.get(i) + "处" + "\n");
                }
            }
            sensNum_jta.append("\n" + "低风险词：" + "\n");
            for (int i = 0; i < wordMap.size(); i++) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) wordMap.entrySet().toArray()[i];
                if ("低风险词".equals(entry.getValue()) && wordNum.get(i) != 0) {
                    sensNum_jta.append(entry.getKey() + ": " + wordNum.get(i) + "处" + "\n");
                }
            }

            jf_result_temp.setVisible(true);
        }
    }

    // 选取敏感词文件并读取内容存入wordMap,wordNum
    public void getLib() {
        //文件选择器选择文件，将文件名显示在标签上
        JFileChooser fChooser = new JFileChooser();
        int ok = fChooser.showOpenDialog(this);//弹出一个open file的显示框，返回值是在显示框上的操作结果
        if (ok != JFileChooser.APPROVE_OPTION) return;//判断是否正常选择，按下确认为approve
        sensWord_jtf.setText(fChooser.getSelectedFile().getPath());

        //清空之前的敏感词列表，将新文件的内容读取到wordList
        wordMap.clear();
        wordNum.clear();
        // 选择敏感词文件
        File choosenLib = fChooser.getSelectedFile();
        BufferedReader br = null;
        // 将敏感词从文件中读取出并存入wordMap,wordNum初始次数为0
        try {
            br = new BufferedReader(new FileReader(choosenLib));
            int flag = 0;
            while (true) {
                String str = br.readLine();
                if (str == null) break;

                if (str.equals("高风险词：")) {
                    flag = 1;
                    continue;
                }
                if (str.equals("中风险词：")) {
                    flag = 2;
                    continue;
                }
                if (str.equals("低风险词：")) {
                    flag = 3;
                    continue;
                }

                if (flag == 1) {
                    wordMap.put(str, "高风险词");//添加到记录中
                    wordNum.add(0);//设置对应的初始值
                }
                if (flag == 2) {
                    wordMap.put(str, "中风险词");//添加到记录中
                    wordNum.add(0);//设置对应的初始值
                }
                if (flag == 3) {
                    wordMap.put(str, "低风险词");//添加到记录中
                    wordNum.add(0);//设置对应的初始值
                }

            }
            br.close();
        } catch (FileNotFoundException e1) {
            JOptionPane.showMessageDialog(null, "文件不存在");
            e1.printStackTrace();
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null, "文件读取失败");
            e1.printStackTrace();
        }
    }

    // 获取HTML源代码
    public String getHtml(String website) {
        String str = null;
        String html = "";//html源代码
        try {
            URL url = new URL(website);
            URLConnection urlConne = url.openConnection();// 创建一个URL连接对象
            urlConne.connect();
            //获取输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConne.getInputStream(), textType));
            System.out.println("crawl start");
            while (true) {
                str = br.readLine();
                if (str == null) break;
                html += (str + "\n");
            }
            br.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, website + "爬取源代码失败");
        }
        System.out.println("crawl end");
        return html;
    }

    public String getText(String html) {

        String html_cleaned = ""; // 纯文本代码

        // 将HTML字符串转化位为doc类型，实体编码也自动转化
        org.jsoup.nodes.Document doc = Jsoup.parse(html, textType);

        // 存取超链接
        Elements links = doc.select("a");
        for (Element link : links) {
            String href = link.attr("href"); // 获取超链接地址
            if (href.startsWith("http://") || href.startsWith("https://")) {
                System.out.println(href);
                hreflist.add(href);
            }
        }

        // 白名单-去除所有标签和属性
        Safelist safelist = Safelist.none();
        html_cleaned = Jsoup.clean(html, safelist);

        // 匹配网页上图案的乱码--其含义是查找包含"&"符号和一系列字符
        // （不包括空白字符）以及一个或多个连续分号（即HTML代码中的HTML实体）的子字符串。通常情况下，这个
        // 正则表达式会匹配HTML中已编码的图像地址，以便可以查找HTML中的图像并对其进行处理或替换。
        html_cleaned = html_cleaned.replaceAll("&[\\S]*?;+", "");

        // 匹配两个或多个连续空格--给内容加上换行
        html_cleaned = html_cleaned.replaceAll("\\s{2,}", "\n");
//        System.out.println(html_cleaned);

        return html_cleaned;
    }

    public void showSensword(JTextArea a) {
        // 设置高亮控件
        Highlighter hg = a.getHighlighter();
        String text = a.getText();
        DefaultHighlighter.DefaultHighlightPainter painter1 = new DefaultHighlighter.DefaultHighlightPainter(new Color(230, 130, 130));
        DefaultHighlighter.DefaultHighlightPainter painter2 = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
        DefaultHighlighter.DefaultHighlightPainter painter3 = new DefaultHighlighter.DefaultHighlightPainter(new Color(200, 200, 200));//设置高亮显示颜色为黄色
        for (String str : wordMap.keySet()) {
            int index = 0;
            // index=查找每一行第一次出现str敏感词的位置  str敏感词 index初始下标
            while ((index = text.indexOf(str, index)) >= 0) {
                try {
                    // 寻找此敏感词存储在wordMap中的位置的下标
                    int i = new ArrayList<>(wordMap.keySet()).indexOf(str);
                    if (i >= 0 && i < wordMap.size()) {
                        // 敏感词次数加一
                        wordNum.set(i, wordNum.get(i) + 1);
                    }
                    if (wordMap.get(str).equals("高风险词")) {
                        hg.addHighlight(index, index + str.length(), painter1);
                    } else if (wordMap.get(str).equals("中风险词")) {
                        hg.addHighlight(index, index + str.length(), painter2);
                    } else if (wordMap.get(str).equals("低风险词")) {
                        hg.addHighlight(index, index + str.length(), painter3);
                    }
                    index += str.length();//更新匹配条件继续匹配
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
