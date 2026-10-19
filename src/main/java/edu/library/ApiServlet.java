package edu.library;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.security.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import com.google.gson.*;
import edu.library.model.*;
import edu.homework.jdbc.util.JDBCTool;

@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {
 private final Gson json=new Gson();
 private static final SecureRandom random=new SecureRandom();
 static class Problem extends RuntimeException {final int code;Problem(int c,String m){super(m);code=c;}}
 static void require(boolean b,String m){if(!b)throw new Problem(400,m);}
 static String str(Map<?,?> m,String k){Object v=m.get(k);return v==null?"":v.toString().trim();}
 static String field(Map<?,?> m,String k,int max){String s=str(m,k);require(!s.isEmpty()&&s.length()<=max,k+" 必填，且长度不超过 "+max);return s;}
 static int integer(Map<?,?> m,String k,int lo,int hi){try{int n=new java.math.BigDecimal(str(m,k)).intValueExact();require(n>=lo&&n<=hi,k+" 超出范围");return n;}catch(NumberFormatException|ArithmeticException e){throw new Problem(400,k+" 必须是整数");}}
 static double price(Map<?,?> m){try{double n=Double.parseDouble(str(m,"price"));require(Double.isFinite(n)&&n>=0&&n<=1000000,"价格不合法");return n;}catch(NumberFormatException e){throw new Problem(400,"请输入有效价格");}}
 static String hash(String password){try{byte[] salt=new byte[16];random.nextBytes(salt);return "pbkdf2$"+Base64.getEncoder().encodeToString(salt)+"$"+derive(password,salt);}catch(Exception e){throw new IllegalStateException(e);}}
 static String derive(String p,byte[] salt)throws Exception{return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(p.toCharArray(),salt,120000,256)).getEncoded());}
 static boolean matches(String p,String saved){try{if(!saved.startsWith("pbkdf2$"))return MessageDigest.isEqual(p.getBytes(java.nio.charset.StandardCharsets.UTF_8),saved.getBytes(java.nio.charset.StandardCharsets.UTF_8));String[] a=saved.split("\\$");return MessageDigest.isEqual(derive(p,Base64.getDecoder().decode(a[1])).getBytes(),a[2].getBytes());}catch(Exception e){return false;}}
 @Override protected void service(HttpServletRequest req,HttpServletResponse res)throws IOException {
  req.setCharacterEncoding("UTF-8");res.setCharacterEncoding("UTF-8");res.setContentType("application/json;charset=UTF-8");res.setHeader("Cache-Control","no-store");res.setHeader("X-Content-Type-Options","nosniff");
  try {
   String path=req.getPathInfo()==null?"":req.getPathInfo();boolean post=req.getMethod().equals("POST");
   require(post||req.getMethod().equals("GET"),"不支持此请求方法");
   Map<String,Object> body=post?json.fromJson(req.getReader(),new com.google.gson.reflect.TypeToken<Map<String,Object>>(){}.getType()):new HashMap<>();if(body==null)body=new HashMap<>();
   if(path.equals("/captcha")){require(!post,"使用 GET");String code=""+(1000+random.nextInt(9000));req.getSession().setAttribute("captcha",code);req.getSession().setAttribute("captchaAt",System.currentTimeMillis());res.setContentType("image/svg+xml");res.getWriter().write("<svg xmlns='http://www.w3.org/2000/svg' width='140' height='46'><rect width='140' height='46' rx='8' fill='#e8eee9'/><path d='M0 12L140 31M0 35L140 8' stroke='#a8bab0'/><text x='24' y='33' font-size='29' letter-spacing='7' fill='#204d3e'>"+code+"</text></svg>");return;}
   try(Connection c=DB.open()){
    Object result;
    if(path.equals("/register")){require(post,"使用 POST");result=register(c,body);}
    else if(path.equals("/login")){require(post,"使用 POST");var s=req.getSession();Object cap=s.getAttribute("captcha");Object at=s.getAttribute("captchaAt");s.removeAttribute("captcha");require(cap!=null&&at!=null&&System.currentTimeMillis()-(Long)at<300000&&cap.equals(str(body,"captcha")),"验证码错误或已过期");var rows=DB.rows(c,"SELECT * FROM user WHERE name=?",str(body,"name"));require(rows.size()==1&&matches(str(body,"password"),String.valueOf(rows.get(0).get("password"))),"用户名或密码错误");var u=rows.get(0);DB.exec(c,"UPDATE user SET loginTime=NOW(),updateTime=NOW() WHERE id=?",u.get("id"));s.invalidate();req.getSession().setAttribute("uid",u.get("id"));result=safe(c,u);}
    else {var session=req.getSession(false);if(session==null||session.getAttribute("uid")==null)throw new Problem(401,"请先登录");var users=DB.rows(c,"SELECT * FROM user WHERE id=?",session.getAttribute("uid"));if(users.isEmpty())throw new Problem(401,"账号不存在");var u=users.get(0);
     if(post){String origin=req.getHeader("Origin");if(origin!=null&&!origin.equals(req.getScheme()+"://"+req.getHeader("Host")))throw new Problem(403,"请求来源不匹配");}
     result=dispatch(c,req,path,post,body,u);
    }
    res.getWriter().write(json.toJson(Map.of("ok",true,"data",result==null?Map.of():result)));
   }
  }catch(Problem e){res.setStatus(e.code);res.getWriter().write(json.toJson(Map.of("ok",false,"message",e.getMessage())));}
  catch(JsonParseException|IllegalArgumentException e){res.setStatus(400);res.getWriter().write(json.toJson(Map.of("ok",false,"message","请求参数格式错误")));}
  catch(Exception e){getServletContext().log("Library request failed",e);res.setStatus(500);res.getWriter().write(json.toJson(Map.of("ok",false,"message","操作未完成，请检查数据库连接或记录关联关系")));}
 }
 Map<String,Object> safe(Connection c,Map<String,Object> u)throws SQLException{var out=new LinkedHashMap<>(u);out.remove("password");String role=String.valueOf(u.get("role"));User user=switch(role){case "管理员"->JDBCTool.getOneById(String.valueOf(u.get("id")),Admin.class,c);case "操作员"->JDBCTool.getOneById(String.valueOf(u.get("id")),Operator.class,c);case "教师"->JDBCTool.getOneById(String.valueOf(u.get("id")),Teacher.class,c);default->JDBCTool.getOneById(String.valueOf(u.get("id")),Student.class,c);};out.put("permissions",user.hasPermission());return out;}
 void role(Map<String,Object> u,String...roles){if(!Arrays.asList(roles).contains(u.get("role")))throw new Problem(403,"当前角色无权执行该操作");}
 Object register(Connection c,Map<String,Object>b)throws Exception{
  String name=field(b,"name",255),phone=field(b,"phone",255),r=field(b,"role",20),pw=field(b,"password",128);require(phone.matches("1[3-9][0-9]{9}"),"请输入有效的11位手机号");require(pw.length()>=6,"密码至少6位");require(List.of("管理员","操作员","学生","教师").contains(r),"角色错误");String major=field(b,"major",255);
  // A MySQL named lock also serializes registrations across servlet instances without altering backup schema.
  require(((Number)DB.rows(c,"SELECT GET_LOCK('LibraryWeb2026.register',10) AS acquired").get(0).get("acquired")).intValue()==1,"注册繁忙，请重试");
  try{require(DB.rows(c,"SELECT id FROM user WHERE name=? OR phone=?",name,phone).isEmpty(),"姓名或手机号已被注册");
   String[] extra=switch(r){case "管理员"->new String[]{"permissionLevel","entryTime"};case "操作员"->new String[]{"area","duty"};case "学生"->new String[]{"grade","college"};default->new String[]{"college","title"};};for(String k:extra)field(b,k,255);
   Object level=r.equals("管理员")?integer(b,"permissionLevel",1,10):null;Object entry=r.equals("管理员")?LocalDate.parse(str(b,"entryTime")):null;
   DB.exec(c,"INSERT INTO user(id,createTime,updateTime,name,password,phone,role,major,permissionLevel,entryTime,area,duty,grade,college,title) VALUES (?,NOW(),NOW(),?,?,?,?,?,?,?,?,?,?,?,?)",DB.id(),name,hash(pw),phone,r,major,level,entry,nullable(b,"area"),nullable(b,"duty"),nullable(b,"grade"),nullable(b,"college"),nullable(b,"title"));return "注册成功，请登录";
  }finally{DB.rows(c,"SELECT RELEASE_LOCK('LibraryWeb2026.register')");}
 }
 Object nullable(Map<String,Object>b,String k){String s=str(b,k);return s.isEmpty()?null:s;}
 interface Work{Object run()throws Exception;}
 Object tx(Connection c,Work f)throws Exception{c.setAutoCommit(false);try{Object o=f.run();c.commit();return o;}catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}}
 Map<String,Object> bookLock(Connection c,String id)throws Exception{var a=DB.rows(c,"SELECT * FROM book WHERE id=? FOR UPDATE",id);require(!a.isEmpty(),"图书不存在");return a.get(0);}
 Object addBook(Connection c,Map<String,Object>b)throws Exception{
  Book book=new Book();book.setId(DB.id());book.setCreateTime(LocalDateTime.now());book.setUpdateTime(LocalDateTime.now());book.setTitle(field(b,"title",30));book.setAuthor(field(b,"author",30));book.setType(field(b,"type",10));book.setPublisher(field(b,"publisher",20));book.setDays(integer(b,"days",1,365));book.setPrice(price(b));book.setStock(integer(b,"stock",1,1000));book.setStatus("正常");book.setIntroduction(str(b,"introduction"));require(str(b,"introduction").length()<=512,"简介最长512字");
  JDBCTool.save(book,c);for(int i=0;i<book.getStock();i++)copy(c,book.getId());return book.getId();
 }
 void copy(Connection c,String book)throws Exception{BookCopy cp=new BookCopy();cp.setId(DB.id());cp.setBookId(book);cp.setCreateTime(LocalDateTime.now());cp.setUpdateTime(LocalDateTime.now());cp.setIsBorrowed(false);JDBCTool.save(cp,c);}
 Object dispatch(Connection c,HttpServletRequest req,String p,boolean post,Map<String,Object>b,Map<String,Object>u)throws Exception{
  String uid=String.valueOf(u.get("id")),id=str(b,"id");String q=Optional.ofNullable(req.getParameter("q")).orElse("").trim();String like="%"+q+"%";
  Set<String> writes=Set.of("/logout","/books/add","/books/edit","/books/discard","/copies/add","/copies/delete","/borrow","/return","/recommend","/review");require(post==writes.contains(p),"请求方法错误");
  switch(p){
   case "/me":return safe(c,u);
   case "/logout":req.getSession().invalidate();return "已退出";
   case "/books":return DB.rows(c,"SELECT b.id,b.createTime,b.title,b.author,b.type,b.publisher,b.days,b.price,b.stock,b.status,b.introduction,(SELECT COUNT(*) FROM bookcopy cp WHERE cp.bookId=b.id AND cp.isBorrowed=0 AND cp.deletedAt IS NULL) AS available FROM book b WHERE title LIKE ? OR author LIKE ? OR type LIKE ? ORDER BY createTime DESC,id DESC",like,like,like);
   case "/users":role(u,"管理员");if(req.getParameter("q")==null)return List.of();return DB.rows(c,"SELECT id,name,role,phone,major,createTime,loginTime,permissionLevel,entryTime,area,duty,grade,college,title FROM user WHERE name LIKE ? OR phone LIKE ? OR major LIKE ? OR role LIKE ? ORDER BY createTime DESC",like,like,like,like);
   case "/books/add":role(u,"操作员");return tx(c,()->addBook(c,b));
   case "/books/edit":role(u,"操作员");return tx(c,()->{bookLock(c,id);require(DB.exec(c,"UPDATE book SET title=?,author=?,type=?,publisher=?,days=?,price=?,updateTime=NOW() WHERE id=?",field(b,"title",30),field(b,"author",30),field(b,"type",10),field(b,"publisher",20),integer(b,"days",1,365),price(b),id)==1,"图书不存在");return "图书已更新";});
   case "/books/discard":role(u,"操作员");return tx(c,()->{bookLock(c,id);require(DB.rows(c,"SELECT id FROM bookcopy WHERE bookId=? AND isBorrowed=1",id).isEmpty(),"仍有外借副本，不能废弃");DB.exec(c,"UPDATE book SET status='已废弃',updateTime=NOW() WHERE id=?",id);return "图书已废弃，借阅记录保留";});
   case "/copies":role(u,"操作员");return DB.rows(c,"SELECT id,bookId,isBorrowed,createTime FROM bookcopy WHERE bookId=? AND deletedAt IS NULL ORDER BY createTime DESC",req.getParameter("bookId"));
   case "/copies/add":role(u,"操作员");return tx(c,()->{var book=bookLock(c,id);require("正常".equals(book.get("status")),"废弃图书不能新增副本");copy(c,id);DB.exec(c,"UPDATE book SET stock=COALESCE(stock,0)+1,updateTime=NOW() WHERE id=?",id);return "已新增副本，库存加1";});
   case "/copies/delete":role(u,"操作员");return tx(c,()->{String bid=field(b,"bookId",36);bookLock(c,bid);var copies=DB.rows(c,"SELECT * FROM bookcopy WHERE id=? AND bookId=? AND deletedAt IS NULL FOR UPDATE",id,bid);require(!copies.isEmpty(),"副本不存在");require(!Boolean.TRUE.equals(copies.get(0).get("isBorrowed")),"外借副本不能删除");DB.exec(c,"UPDATE bookcopy SET deletedAt=NOW(),updateTime=NOW() WHERE id=?",id);DB.exec(c,"UPDATE book SET stock=GREATEST(COALESCE(stock,0)-1,0),updateTime=NOW() WHERE id=?",bid);return "已删除副本，库存减1";});
   case "/borrow":return tx(c,()->{var book=bookLock(c,id);require("正常".equals(book.get("status")),"图书已废弃");var cps=DB.rows(c,"SELECT id FROM bookcopy WHERE bookId=? AND isBorrowed=0 AND deletedAt IS NULL ORDER BY id LIMIT 1 FOR UPDATE",id);require(!cps.isEmpty(),"暂无可借副本");String cp=String.valueOf(cps.get(0).get("id"));Loan l=new Loan();l.setId(DB.id());l.setCreateTime(LocalDateTime.now());l.setUpdateTime(LocalDateTime.now());l.setBookCopyId(cp);l.setUserId(uid);l.setBorrowDate(LocalDateTime.now());int days=((Number)book.get("days")).intValue();require(days>0,"图书借阅期限无效，请联系操作员");l.setDueDate(l.getBorrowDate().plusDays(days));l.setStatus("未还");JDBCTool.save(l,c);DB.exec(c,"UPDATE bookcopy SET isBorrowed=1,updateTime=NOW() WHERE id=?",cp);return "借阅成功，副本编号："+cp;});
   case "/return":return tx(c,()->{var found=DB.rows(c,"SELECT bookId FROM bookcopy WHERE id=?",id);require(!found.isEmpty(),"副本不存在");bookLock(c,String.valueOf(found.get(0).get("bookId")));var loans=DB.rows(c,"SELECT id FROM loan WHERE bookCopyId=? AND userId=? AND returnDate IS NULL FOR UPDATE",id,uid);require(!loans.isEmpty(),"没有属于你的未还记录");DB.exec(c,"UPDATE loan SET returnDate=NOW(),status='已还',updateTime=NOW() WHERE id=?",loans.get(0).get("id"));DB.exec(c,"UPDATE bookcopy SET isBorrowed=0,updateTime=NOW() WHERE id=?",id);return "归还成功";});
   case "/mine":case "/loans":{
    boolean all=p.equals("/loans");if(all)role(u,"管理员");StringBuilder sql=new StringBuilder("SELECT l.id,l.bookCopyId,l.borrowDate,l.returnDate,l.dueDate,CASE WHEN l.returnDate IS NOT NULL THEN '已还' WHEN l.dueDate<NOW() THEN '超期' ELSE '未还' END AS status,b.title,u.name,u.major FROM loan l JOIN bookcopy cp ON cp.id=l.bookCopyId JOIN book b ON b.id=cp.bookId JOIN user u ON u.id=l.userId WHERE 1=1");List<Object>a=new ArrayList<>();if(!all){sql.append(" AND l.userId=?");a.add(uid);}sql.append(" AND (u.name LIKE ? OR b.title LIKE ?)");a.add(like);a.add(like);
    String mode=req.getParameter("mode");if("unreturned".equals(mode))sql.append(" AND l.returnDate IS NULL");if("overdue".equals(mode))sql.append(" AND l.returnDate IS NULL AND l.dueDate<NOW()");String start=req.getParameter("start"),end=req.getParameter("end");if(start!=null&&!start.isBlank()){sql.append(" AND l.borrowDate>=?");a.add(LocalDate.parse(start).atStartOfDay());}if(end!=null&&!end.isBlank()){if(start!=null&&!start.isBlank())require(!LocalDate.parse(end).isBefore(LocalDate.parse(start)),"结束日期不能早于开始日期");sql.append(" AND l.borrowDate<?");a.add(LocalDate.parse(end).plusDays(1).atStartOfDay());}sql.append(" ORDER BY l.borrowDate DESC");return DB.rows(c,sql.toString(),a.toArray());}
   case "/stats":role(u,"管理员");return Map.of("types",DB.rows(c,"SELECT b.type AS label,COUNT(l.id) AS count FROM book b LEFT JOIN bookcopy cp ON cp.bookId=b.id LEFT JOIN loan l ON l.bookCopyId=cp.id GROUP BY b.type ORDER BY b.type"),"majors",DB.rows(c,"SELECT u.major AS label,COUNT(l.id) AS count FROM user u LEFT JOIN loan l ON l.userId=u.id GROUP BY u.major ORDER BY count DESC"));
   case "/recommendations":role(u,"教师","管理员");return "管理员".equals(u.get("role"))?DB.rows(c,"SELECT r.*,u.name FROM bookrecommend r JOIN user u ON u.id=r.userId ORDER BY r.createTime DESC"):DB.rows(c,"SELECT * FROM bookrecommend WHERE userId=? ORDER BY createTime DESC",uid);
   case "/recommend":role(u,"教师");return tx(c,()->{String title=field(b,"title",20),author=field(b,"author",20),publisher=field(b,"publisher",25);require(DB.rows(c,"SELECT id FROM book WHERE title=?",title).isEmpty(),"馆内已有同名图书，请先查询图书馆藏");DB.exec(c,"INSERT INTO bookrecommend(id,createTime,updateTime,userId,bookName,author,publisher,price,status) VALUES (?,NOW(),NOW(),?,?,?,?,?,'待审核')",DB.id(),uid,title,author,publisher,price(b));return "推荐已提交审核";});
   case "/review":role(u,"管理员");return tx(c,()->{var rs=DB.rows(c,"SELECT * FROM bookrecommend WHERE id=? FOR UPDATE",id);require(!rs.isEmpty()&&"待审核".equals(rs.get(0).get("status")),"推荐不存在或已审核");boolean accept="approve".equals(str(b,"decision"));if(accept){var r=rs.get(0);require(DB.rows(c,"SELECT id FROM book WHERE title=?",r.get("bookName")).isEmpty(),"同名图书已入馆，请驳回重复推荐");Map<String,Object> nb=new HashMap<>(b);nb.put("title",r.get("bookName"));nb.put("author",r.get("author"));nb.put("publisher",r.get("publisher"));nb.put("price",r.get("price"));addBook(c,nb);}else{require("reject".equals(str(b,"decision")),"审核操作无效");field(b,"reason",100);}DB.exec(c,"UPDATE bookrecommend SET status=?,reason=?,updateTime=NOW() WHERE id=?",accept?"已通过":"已驳回",accept?"已加入馆藏":str(b,"reason"),id);return accept?"审核通过并已加入馆藏":"已驳回推荐";});
   default:throw new Problem(404,"接口不存在");
  }
 }
}
