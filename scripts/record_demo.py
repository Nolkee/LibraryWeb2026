#!/usr/bin/env python3
"""Record real browser interactions through the web-access CDP proxy.
Requires proxy on localhost:3456 and a dedicated task-created browser tab.
"""
import urllib.request,urllib.parse,json,time,sys,pathlib,subprocess,os
TARGET=sys.argv[1];ROOT=pathlib.Path(__file__).resolve().parents[1];FRAMES=ROOT/'.runtime/demo-frames';FRAMES.mkdir(parents=True,exist_ok=True)
PROXY='http://localhost:3456';frame=0;timeline=[];started=time.time()
def request(route,data=None):
 r=urllib.request.Request(PROXY+route,data=data.encode() if data is not None else None)
 with urllib.request.urlopen(r,timeout=20) as out:return out.read()
def js(code):
 out=json.loads(request('/eval?target='+TARGET,code))
 if 'error' in out:raise RuntimeError(out)
 return out.get('value')
def caption(text):
 js("(()=>{let e=document.getElementById('recording-caption');if(!e){e=document.createElement('div');e.id='recording-caption';e.style='position:fixed;bottom:0;left:0;right:0;padding:9px 24px;background:#183d32;color:#fff;z-index:999999;font:14px sans-serif;display:flex;justify-content:space-between';document.body.append(e);}e.textContent="+json.dumps(text+'  |  '+ '实际访问地址：')+"+location.href;return true;})()")
 timeline.append((round(frame/2,1),text));(ROOT/'.runtime/demo-timeline-progress.json').write_text(json.dumps(timeline,ensure_ascii=False,indent=2));print(text,flush=True)
def hold(seconds=2):
 global frame
 for _ in range(int(seconds*2)):
  tick=time.monotonic();content=request('/screenshot?target='+TARGET)
  (FRAMES/f'{frame:05d}.png').write_bytes(content);frame+=1
  time.sleep(max(0,.5-(time.monotonic()-tick)))
def click(selector):js('document.querySelector('+json.dumps(selector)+').click()');time.sleep(.6)
def fill(values,form=None):
 prefix=(form+' ') if form else ''
 for name,value in values.items():
  selector=prefix+'[name="'+name+'"]'
  js('(()=>{let e=document.querySelector('+json.dumps(selector)+');if(!e)throw Error("missing input");e.value='+json.dumps(str(value))+';e.dispatchEvent(new Event("change",{bubbles:true}));return true;})()')
def submit(selector):js('document.querySelector('+json.dumps(selector)+').requestSubmit()');time.sleep(.8)
def nav(page):click('[data-page="'+page+'"]')
def search(q):fill({'q':q},'#search');submit('#search')
def stage(text,seconds=2):caption(text);hold(seconds)
def login(name,password):
 if js('!document.querySelector("#shell").hidden'):click('#logout')
 click('#loginTab');fill({'name':name,'password':password},'#authForm')
 code=js(r'(async()=>{let svg=await(await fetch("api/captcha")).text();return svg.match(/>(\d{4})<\/text>/)[1];})()');fill({'captcha':code},'#authForm');stage('用户名、密码与一次性验证码登录',1);submit('#authForm');assert js('!document.querySelector("#shell").hidden'),js('document.querySelector("#authError").textContent')
# A new capture starts with an empty task-generated frame directory.
for generated_frame in FRAMES.glob('*.png'):generated_frame.unlink()
# Check page structure before acting. This script is specific to this project's observed DOM.
assert js('Boolean(document.querySelector("#authForm")&&document.querySelector("#nav"))')
if js('!document.querySelector("#shell").hidden'):click('#logout')
# Remove only a browser extension's generated color override for recording intended site colors.
js('document.querySelectorAll("style.darkreader").forEach(e=>e.remove())')
stage('作业2 · Maven WAR / Servlet + JDBC / MySQL / Tomcat 10.1',3)
click('#registerTab');stage('注册：验证手机号，按角色显示不同信息项',2)
for role in ['教师','操作员','管理员','学生']:
 fill({'role':role},'#authForm');stage('注册角色：'+role+'，显示对应子类属性',1.5)
marker='演示读者'+str(int(time.time()))[-6:]
fill(dict(name=marker,password='demo2026',phone='139'+str(int(time.time()))[-8:],major='软件工程',grade='2026级',college='信息学院'),'#authForm');stage('填写学生注册信息',2);submit('#authForm');stage('注册成功，返回登录页面',2)
login('operator','operator123');stage('操作员首页：图书维护、借阅与归还权限',2)
nav('books');stage('加载附件备份中的全部馆藏，支持反复添加图书',2)
click('#addBook');book='Web实践演示'+str(int(time.time()))[-5:]
fill(dict(title=book,author='课程实践组',type='计算机',publisher='课程出版社',days=14,price=39.8,stock=2,introduction='录屏演示用图书，新增时自动生成副本。'),'#bookForm');stage('新增图书：库存为2，提交后自动生成2个副本',2);submit('#bookForm');stage('新增图书立即显示在列表首行',2)
search(book);click('[data-act="edit"]');fill(dict(title=book,author='课程实践组',type='计算机',publisher='课程出版社',days=21,price=42),'#bookForm');stage('编辑图书：仅修改书名、作者、类型、出版社、期限与价格',2);submit('#bookForm');search(book)
click('[data-act="copies"]');stage('副本管理：2个自动生成的副本',2);click('#addCopy');stage('新增副本后，库存同步加1',2);js('window.confirm=()=>true');click('[data-delete]');stage('删除在馆副本后，库存同步减1',2);click('#closeDialog')
nav('borrow');search(book);click('[data-act="borrow"]');stage('借阅成功：可借数量减少，自己的未还记录立即出现',3)
nav('books');search(book);click('[data-act="discard"]');stage('仍有外借副本，系统阻止废弃图书',2)
nav('return');search(book);stage('归还：通过当前用户的图书副本编号操作',2);click('[data-return]');stage('归还成功，记录从未还列表中消失',2)
nav('books');search(book);click('[data-act="discard"]');stage('副本全部归还后允许废弃，历史借阅记录保留',2)
login('student1','student123');stage('学生首页：仅显示借书、还书、个人借阅功能',2)
nav('borrow');stage('使用附件已有馆藏数据进行借阅',2)
existing=js('(()=>{let b=document.querySelector("[data-act=borrow]");return b?b.dataset.id:null;})()');assert existing
existing_title=js('(async()=>{let rows=(await(await fetch("api/books?q=")).json()).data;return rows.find(b=>b.id==='+json.dumps(existing)+').title;})()')
click('[data-act="borrow"][data-id="'+existing+'"]');stage('学生借阅成功，实时更新个人借阅记录',2)
nav('mine');stage('个人借阅：全部、未还、超期及日期范围查询',2)
nav('return');search(existing_title);stage('学生按副本编号归还原有馆藏',2);click('[data-return]');stage('归还已有馆藏图书，完成学生借还闭环',2)
login('teacher','teacher123');stage('教师首页：增加推荐图书功能',2)
nav('recommend');click('#recommend');rec='推荐演示'+str(int(time.time()))[-5:];fill(dict(title=rec,author='课程推荐组',publisher='课程出版社',price=58),'#recommendForm');stage('推荐图书：馆内无同名图书时提交审核',2);submit('#recommendForm');stage('教师查看推荐状态：待审核',2)
click('#recommend');rec2='驳回演示'+str(int(time.time()))[-5:];fill(dict(title=rec2,author='课程推荐组',publisher='课程出版社',price=58),'#recommendForm');submit('#recommendForm')
login('admin','admin123');stage('管理员首页：用户查询、记录、统计与推荐审核',2)
nav('users');stage('用户查询默认不显示任何数据',2);search('软件');stage('关键字模糊查询，按创建时间倒序显示',2);click('[data-user]');stage('用户详情弹窗，仅查看，不提供增删改',2);click('#closeDialog')
nav('loans');stage('全馆借阅查询：显示原始数据和本次演示记录',2);fill({'mode':'overdue'},'#search');submit('#search');stage('超期借阅：实时按应还时间判定',2)
fill({'mode':'','start':'2026-06-01','end':'2026-06-30'},'#search');submit('#search');stage('按借阅日期范围查询',2)
nav('stats');stage('Vue统计视图：全部图书类型与专业借阅次数',3)
nav('review')
# Inspect observed rows and choose the pending recommendation by visible title.
js(r'(()=>{let row=Array.from(document.querySelectorAll("#content tbody tr")).find(r=>r.innerText.replace(/\s/g,"").includes('+json.dumps(rec)+'));row.querySelector("button").click();})()');time.sleep(.5)
fill(dict(type='计算机',days=30,stock=1),'#reviewForm');stage('审阅推荐：通过时设置类型、期限与库存',2);submit('#reviewForm');stage('审核通过，图书及其副本自动加入馆藏',2)
js(r'(()=>{let row=Array.from(document.querySelectorAll("#content tbody tr")).find(r=>r.innerText.replace(/\s/g,"").includes('+json.dumps(rec2)+'));row.querySelector("button").click();})()');time.sleep(.5)
fill({'decision':'reject'},'#reviewForm');fill({'reason':'暂不符合本次课程馆藏采购计划'},'#reviewForm');stage('驳回推荐必须填写理由',2);submit('#reviewForm');stage('审核完成，保留审核结果与驳回理由',2)
nav('borrow');search(rec);stage('审核通过的推荐图书已可借阅',2)
nav('home');stage('演示完成 · 同一WAR提供静态页面和Servlet接口 · 无JSP',3)
(ROOT/'docs').mkdir(exist_ok=True)
(ROOT/'docs/demo-timeline.json').write_text(json.dumps(timeline,ensure_ascii=False,indent=2))
print('Encoding '+str(frame)+' captured browser frames',flush=True)
out=ROOT/'target/LibraryWeb2026-演示视频.mp4'
subprocess.run(['ffmpeg','-y','-hide_banner','-loglevel','error','-framerate','2','-i',str(FRAMES/'%05d.png'),'-vf','scale=1414:786,fps=20,format=yuv420p','-c:v','libx264','-preset','fast','-crf','22','-movflags','+faststart',str(out)],check=True)
print('VIDEO '+str(out),flush=True)
