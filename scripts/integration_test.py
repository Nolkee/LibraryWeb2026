#!/usr/bin/env python3
"""Runs against deployed Tomcat + restored MySQL, and cleans up its own fixture rows."""
import urllib.request,urllib.error,http.cookiejar,json,re,sys,uuid,subprocess,os
BASE=os.getenv('LIBRARY_TEST_URL','http://127.0.0.1:8080/LibraryWeb2026')
checks=0
class Client:
 def __init__(self):self.http=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
 def call(self,path,data=None,status=200):
  global checks
  request=urllib.request.Request(BASE+'/api'+path,data=json.dumps(data).encode() if data is not None else None,headers={'Content-Type':'application/json'} if data is not None else {})
  try:r=self.http.open(request)
  except urllib.error.HTTPError as e:r=e
  raw=r.read().decode();assert r.status==status,(path,r.status,raw)
  checks+=1
  return json.loads(raw).get('data') if status==200 else raw
 def login(self,name,pw):
  svg=self.http.open(BASE+'/api/captcha').read().decode();cap=re.search(r'>(\d{4})</text>',svg).group(1)
  return self.call('/login',dict(name=name,password=pw,captcha=cap))
admin,op,student,teacher,guest=[Client() for _ in range(5)]
marker='IT'+uuid.uuid4().hex[:8]
try:
 guest.call('/books',status=401)
 admin.login('admin','admin123');op.login('operator','operator123');student.login('student1','student123');teacher.login('teacher','teacher123')
 assert 'password' not in admin.call('/me')
 assert len(op.call('/books?q='))>=54
 assert admin.call('/users')==[]
 assert len(admin.call('/users?q=admin'))>=1
 student.call('/users?q=',status=403);teacher.call('/stats',status=403)
 for role,extra in [('学生',dict(grade='2026级',college='测试学院')),('教师',dict(college='测试学院',title='讲师')),('操作员',dict(area='测试区',duty='测试员')),('管理员',dict(permissionLevel=1,entryTime='2026-10-19'))]:
  name=marker+role;payload=dict(name=name,password='test123456',phone='139'+str(uuid.uuid4().int)[:8],role=role,major='测试专业',**extra)
  guest.call('/register',payload);guest.call('/register',payload,status=400)
  fresh=Client();assert fresh.login(name,'test123456')['role']==role
 guest.call('/register',dict(name=marker,phone='123',password='test123456',role='学生',major='测试',grade='1',college='测试'),status=400)
 data=dict(title=marker,author='测试作者',type='计算机',publisher='测试出版社',days=7,price=29.5,stock=2,introduction='测试临时图书')
 student.call('/books/add',data,status=403)
 bid=op.call('/books/add',data);copies=op.call('/copies?bookId='+bid);assert len(copies)==2
 assert op.call('/books?q='+marker)[0]['id']==bid
 op.call('/books/edit',dict(data,id=bid,title=marker+'修订',stock=999,status='已废弃'));book=op.call('/books?q='+marker)[0];assert book['stock']==2 and book['status']=='正常'
 op.call('/copies/add',dict(id=bid));copies=op.call('/copies?bookId='+bid);assert len(copies)==3
 op.call('/copies/delete',dict(id=copies[0]['id'],bookId=bid));assert len(op.call('/copies?bookId='+bid))==2
 student.call('/borrow',dict(id=bid));mine=student.call('/mine?mode=unreturned');loan=next(x for x in mine if x['title']==marker+'修订');cp=loan['bookCopyId'];assert loan['status']=='未还'
 op.call('/books/discard',dict(id=bid),status=400);op.call('/copies/delete',dict(id=cp,bookId=bid),status=400)
 teacher.call('/return',dict(id=cp),status=400);student.call('/return',dict(id=cp));student.call('/return',dict(id=cp),status=400)
 assert not any(x['bookCopyId']==cp for x in student.call('/mine?mode=unreturned'))
 op.call('/copies/delete',dict(id=cp,bookId=bid));assert any(x['bookCopyId']==cp for x in student.call('/mine'))
 op.call('/books/discard',dict(id=bid));student.call('/borrow',dict(id=bid),status=400)
 assert admin.call('/loans?q='+marker)
 admin.call('/loans?mode=overdue');admin.call('/loans?start=2026-01-01&end=2026-12-31');admin.call('/loans?start=2026-12-31&end=2026-01-01',status=400)
 assert len(admin.call('/stats')['types'])>=7
 teacher.call('/recommend',dict(data,title=marker+'推荐'));teacher.call('/recommend',dict(data,title=marker+'修订'),status=400)
 rec=next(r for r in admin.call('/recommendations') if r['bookName']==marker+'推荐')
 student.call('/review',dict(id=rec['id'],decision='approve',type='文学',days=14,stock=1),status=403)
 admin.call('/review',dict(id=rec['id'],decision='approve',type='文学',days=14,stock=1))
 admin.call('/review',dict(id=rec['id'],decision='approve',type='文学',days=14,stock=1),status=400)
 teacher.call('/recommend',dict(data,title=marker+'驳回'));rec=next(r for r in admin.call('/recommendations') if r['bookName']==marker+'驳回')
 admin.call('/review',dict(id=rec['id'],decision='reject',reason=''),status=400);admin.call('/review',dict(id=rec['id'],decision='reject',reason='不符合采购计划'))
 admin.call('/logout',{});admin.call('/me',status=401)
 print(f'PASS: {checks} HTTP assertions; registration, permissions, CRUD, copy history, borrow/return, recommendations, statistics')
finally:
 # Restrict cleanup to the unique generated marker. Preserve every original backup record.
 sql=f"SET FOREIGN_KEY_CHECKS=0; DELETE l FROM loan l JOIN bookcopy cp ON cp.id=l.bookCopyId JOIN book b ON b.id=cp.bookId WHERE b.title LIKE '{marker}%'; DELETE cp FROM bookcopy cp JOIN book b ON b.id=cp.bookId WHERE b.title LIKE '{marker}%'; DELETE FROM book WHERE title LIKE '{marker}%'; DELETE FROM bookrecommend WHERE bookName LIKE '{marker}%'; DELETE FROM user WHERE name LIKE '{marker}%'; SET FOREIGN_KEY_CHECKS=1;"
 subprocess.run(['mysql','--protocol=TCP','-h127.0.0.1','-P3307','-uroot','LibraryWeb2026','-e',sql],check=True)
