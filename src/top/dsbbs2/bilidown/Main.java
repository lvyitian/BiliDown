package top.dsbbs2.bilidown;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class Main extends JFrame {
	static {
		System.setProperty("java.net.useSystemProxies", "true");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5469784908345879191L;
	private JPanel contentPane;
	private JTextField textField;
	public volatile ScheduledExecutorService tmp=Executors.newSingleThreadScheduledExecutor();
	public volatile String lastText;
	public volatile long shouldEnabled;
	public volatile boolean parsed;
	public final Vector<String> links=new Vector<>();
	public final Object lock=new Object();
	public final Object lock2=new Object();
	private JComboBox<String> comboBox;
	private JTextArea textArea;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField = new JTextField();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				synchronized(lock2)
				{
					//System.out.println(e);
					synchronized(lock) {
					  shouldEnabled--;
					}
					comboBox.setEnabled(shouldEnabled>=0);
					long tmp2=tmp.shutdownNow().size();
					try {
					  tmp.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
					} catch (Throwable e1) {
						throw new RuntimeException(e1);
					}finally {
						synchronized(lock)
						{
							if(shouldEnabled<0)
							{
								shouldEnabled+=tmp2;
								if(shouldEnabled>0)
								  shouldEnabled=0;
							}
						}
						synchronized(lock)
						{
							if(shouldEnabled>0)
								  shouldEnabled=0;
						}
						comboBox.setEnabled(shouldEnabled>=0);
					}
					parsed=false;
					tmp=Executors.newSingleThreadScheduledExecutor();
					tmp.schedule(()->{
						try {
							synchronized(lock2)
							{
								if(!Objects.equals(lastText,textField.getText()))
								{
								  lastText=textField.getText();
								  //System.out.println(textField.getText());
								  comboBox.removeAllItems();
								  textArea.setText("");
								  String type=null;
								  String id=null;
								  String url=null;
								  if(textField.getText().contains("BV")||textField.getText().contains("bv"))
								  {
									  type="BV";
									  try {
									  StringBuilder app=new StringBuilder("");
									  String[] tmp;
									  if(textField.getText().indexOf("bv")!=-1&&textField.getText().indexOf("bv")<textField.getText().indexOf("BV"))
										  tmp=textField.getText().split("bv");
									  else
										  tmp=textField.getText().split("BV");
									  if(tmp.length<2)
										  return;
									  for(int i=1;i<tmp.length;i++)
										 app.append(tmp[i]);
									  id="BV"+app.toString();
									  }catch(Throwable exc) {return;}
									  url="https://www.bilibili.com/video/"+id;
								  }else if(textField.getText().contains("av"))
								  {
									  type="av";
									  try {
									  id="av"+textField.getText().split("av")[1];
									  }catch(Throwable exc) {return;}
									  url="https://www.bilibili.com/video/"+id;
								  }else if(textField.getText().contains("ep"))
								  {
									  type="ep";
									  try {
									  id="ep"+textField.getText().split("ep")[1];
									  }catch(Throwable exc) {return;}
									  url="https://www.bilibili.com/bangumi/play/"+id;
								  }else if(textField.getText().contains("ss"))
								  {
									  type="ss";
									  try {
									  id="ss"+textField.getText().split("ss")[1];
									  }catch(Throwable exc) {return;}
									  url="https://www.bilibili.com/bangumi/play/"+id;
								  }
								  if(url!=null&&!url.trim().equals(""))
								  {
									  try {
									  String buf;
									  URLConnection con=new URL(url).openConnection();
									  con.setDoOutput(false);
									  con.setDoInput(true);
									  con.setAllowUserInteraction(true);
									  con.connect();
									  try(InputStream i=Objects.equals(Optional.ofNullable(con.getContentEncoding()).orElse("").toLowerCase(),"gzip")?new GZIPInputStream(con.getInputStream()):con.getInputStream())
									  {
										  try(ByteArrayOutputStream bao=new ByteArrayOutputStream())
										  {
											  while(true)
											  {
												  int tmpbyt=i.read();
												  if(tmpbyt==-1)
													  break;
												  bao.write(tmpbyt);
											  }
											  buf=new String(bao.toByteArray(),StandardCharsets.UTF_8);
										  }
									  }
									  ScriptEngine tmpeng=new ScriptEngineManager().getEngineByExtension("js");
									  tmpeng.put("result", buf);
									  tmpeng.eval("result=new String(result);var data=result.match(/__INITIAL_STATE__=(.*?);\\(function\\(\\)/)[1];data=JSON.parse(new String(data));");
									  tmpeng.eval("java.lang.System.out.println(\"INITIAL STATE: \"+JSON.stringify(data));");
									  //long aid;
									  long pid;
									  long cid;
									  if(Objects.equals(type, "BV")||Objects.equals(type, "av"))
									  {
										  //aid=(long)(int)tmpeng.eval("data.videoData.aid;");
										  tmpeng.put("url", url);
										  tmpeng.eval("url=new String(url);");
										  pid=(long)(int)tmpeng.eval("parseInt(url.split(\"p=\")[1], 10) || 1;");
										  tmpeng.put("pid", pid);
										  cid=(long)(int)tmpeng.eval("data.videoData.pages[pid - 1].cid;");
									  }else if(Objects.equals(type, "ep"))
									  {
										  //aid=(long)(int)tmpeng.eval("data.epInfo.aid;");
										  cid=(long)(int)tmpeng.eval("data.epInfo.cid;");
									  }else if(Objects.equals(type, "ss"))
									  {
										  //aid=(long)(int)tmpeng.eval("data.epList[0].aid;");
										  cid=(long)(int)tmpeng.eval("data.epList[0].cid;");
									  }else throw new Throwable();
									  getData(cid, type);
									  }catch(Throwable exc) {exc.printStackTrace();throw new RuntimeException(exc);}
								  }
								}
							}
						}finally {
							synchronized(lock) {
							  shouldEnabled++;
							}
							comboBox.setEnabled(shouldEnabled>=0);
							synchronized(lock2) {
							  parsed=true;
							}
						}
					},685,TimeUnit.MILLISECONDS);
				}
			}
		});
		textField.setBounds(74, 30, 350, 35);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JLabel lblWebUrl = new JLabel("Web URL:");
		lblWebUrl.setBounds(10, 40, 54, 15);
		contentPane.add(lblWebUrl);
		
		comboBox = new JComboBox<>();
		comboBox.setBounds(94, 85, 297, 21);
		contentPane.add(comboBox);
		
		JLabel lblResolution = new JLabel("Resolution:");
		lblResolution.setBounds(10, 88, 84, 15);
		contentPane.add(lblResolution);
		
		JButton btnGetVideoUrl = new JButton("Get Video URL");
		btnGetVideoUrl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!parsed||comboBox.getItemCount()<=0||comboBox.getSelectedItem()==null||Objects.equals(comboBox.getSelectedItem(),""))
					return;
				textArea.setText(links.get(comboBox.getSelectedIndex()));
			}
		});
		btnGetVideoUrl.setBounds(94, 122, 239, 23);
		contentPane.add(btnGetVideoUrl);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBounds(0, 0, 414, 102);
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 155, 414, 96);
		contentPane.add(scrollPane);
	}
	public void getData(long cid,String type) throws Throwable
	{
		getData(false,cid,type);
	}
	public void getData(boolean fallback,long cid,String type) throws Throwable
	{
		String playUrl;
		if(fallback)
		{
			String params="cid="+cid+"&module=movie&player=1&quality=112&ts=1";
			String sign=new BigInteger(1,MessageDigest.getInstance("MD5").digest((params + "9b288147e5474dd2aa67085f716c560d").getBytes(StandardCharsets.UTF_8))).toString(16);
			while(sign.length()<32)
				sign="0"+sign;
			playUrl = "https://bangumi.bilibili.com/player/web_api/playurl?"+params+"&sign="+sign;
		}else {
			if(Objects.equals(type, "BV")||Objects.equals(type, "av"))
			{
				String params="appkey=iVGUTjsxvpLeuDCf&cid="+cid+"&otype=json&qn=112&quality=112&type=";
				String sign=new BigInteger(1,MessageDigest.getInstance("MD5").digest((params + "aHRmhWMLkdeMuILqORnYZocwMBpMEOdt").getBytes(StandardCharsets.UTF_8))).toString(16);
				while(sign.length()<32)
					sign="0"+sign;
				playUrl = "https://interface.bilibili.com/v2/playurl?"+params+"&sign="+sign;
			}else {
				playUrl = "https://api.bilibili.com/pgc/player/web/playurl?qn=80&cid="+cid;
			}
		}
		  String buf;
		  URLConnection con=new URL(playUrl).openConnection();
		  con.setDoOutput(false);
		  con.setDoInput(true);
		  con.setAllowUserInteraction(true);
		  con.connect();
		  try(InputStream i=Objects.equals(Optional.ofNullable(con.getContentEncoding()).orElse("").toLowerCase(),"gzip")?new GZIPInputStream(con.getInputStream()):con.getInputStream())
		  {
			  try(ByteArrayOutputStream bao=new ByteArrayOutputStream())
			  {
				  while(true)
				  {
					  int tmpbyt=i.read();
					  if(tmpbyt==-1)
						  break;
					  bao.write(tmpbyt);
				  }
				  buf=new String(bao.toByteArray(),StandardCharsets.UTF_8);
			  }
		  }
		  if(fallback)
		  {
			  textArea.setText(buf);
			  return;
		  }
		  ScriptEngine tmpeng=new ScriptEngineManager().getEngineByExtension("js");
		  tmpeng.put("result", buf);
		  tmpeng.eval("result=new String(result);var data=JSON.parse(result);var target=data.durl || data.result.durl;java.lang.System.out.println(\"PLAY URL: \"+JSON.stringify(data));");
		  if(tmpeng.eval("target;")!=null)
		  {
			  links.clear();
			  tmpeng.eval("var quality=data.quality || data.result.quality;var qualityArray={112: \"高清 1080P+\",80: \"高清 1080P\",74: \"高清 720P60\",64: \"高清 720P\",48: \"高清 720P\",32: \"清晰 480P\",16: \"流畅 360P\",15: \"流畅 360P\"};");
			  tmpeng.put("comboBox", comboBox);
			  tmpeng.eval("comboBox.addItem(qualityArray[quality]||\"未知\");");
			  if(fallback)
				  throw new Throwable();
			  tmpeng.put("links", links);
			  tmpeng.eval("target.forEach(function(part){links.add(part.url);});");
		  }else {
			  if(fallback)
				  throw new Throwable();
			  getData(true, cid, type);
		  }
	}
}
