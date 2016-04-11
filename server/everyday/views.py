from My365 import settings

from django.shortcuts import render_to_response
from django.template.context import RequestContext
from everyday.forms import ImageUploadForm
import os

import time
import everyday
import datetime
from django.http import HttpResponse

from PIL import Image
from sae.storage import Bucket

from StringIO import StringIO

def index(request):
    imagelist = everyday.models.Image.objects.order_by("-uploadtime")[0:0]
    return render_to_response('index.html',{'imagelist':imagelist,})

def show_more(request,id):
    int_id = int(id)
    imagelist = everyday.models.Image.objects.order_by("-uploadtime")[int_id:int_id+0]
    return render_to_response('items.html',{'imagelist':imagelist,})

def show_image(request,id):
    image = everyday.models.Image.objects.get(id=id)
    return render_to_response('detail.html',{'image':image,
                                             'days':get_days(image.uploadtime),})

def image_upload(request):
    #request page
    if request.method =='GET':
        form = ImageUploadForm()
        path = os.path.abspath(os.path.join(settings.ROOT_PATH, os.pardir))
        return render_to_response('upload.html',{'form':form,'path':path},RequestContext(request))
    #upload image
    elif request.method == 'POST':
        if request.POST['loginCode']!='776655':
            raise Exception
        form = ImageUploadForm(request.POST,request.FILES)
        if form.is_valid(): 
            today = time.strftime('%Y%m%d',time.localtime(time.time()))
            filename = '1_'+today+'.jpg'
            thumbnail(request,filename)
            save_db(request,filename)
            return HttpResponse("success")
        
def save_db(request,filename):
    now = datetime.datetime.now()
    hasImage = everyday.models.Image.objects.filter(uploadtime=now)
    if len(hasImage)==1:#has upload
        image = hasImage[0]
        image.mood=request.POST['mood']
        image.remark=request.POST['remark']
        image.save()
    elif len(hasImage)==0:#not upload
        image = everyday.models.Image(imagename=filename,location='',mood=request.POST['mood'],remark=request.POST['remark'])
        image.save()
    else:
        raise Exception
    
    
def thumbnail(request,filename):
    bucket = Bucket('upload')
    bucket.put()
    bucket.put_object("image/"+filename, request.FILES['file'])

    obj = bucket.get_object_contents("image/"+filename)
    image = Image.open(StringIO(obj))
    image.thumbnail((160,120),Image.ANTIALIAS)
    imgOut = StringIO()
    image.save(imgOut, 'jpeg')
    img_data = imgOut.getvalue()
    bucket.put_object('thumbnail/'+filename, img_data)
    imgOut.close()

def get_days(ends):
    birth = time.strptime('1989-11-09', "%Y-%m-%d")
    begins = datetime.date(*birth[:3])
    days= ends-begins
    return days.days