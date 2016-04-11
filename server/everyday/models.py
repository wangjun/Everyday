from django.db import models

# Create your models here.
class Image(models.Model):
    imagename = models.CharField(max_length=60)
    uploadtime = models.DateField(auto_now=True)
    location = models.CharField(max_length=100)
    mood = models.CharField(max_length=5)
    remark = models.CharField(max_length=140)
    

    def __unicode__(self):
        return self.imagename


class Set(models.Model):
    key = models.CharField(max_length=100,primary_key=True)
    value = models.CharField(max_length=100)
    
    def __unicode__(self):
        return self.key+':'+self.value