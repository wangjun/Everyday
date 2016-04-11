from django import forms

class ImageUploadForm(forms.Form):
    file = forms.FileField(label='image')
    mood = forms.CharField(max_length=5,label='mood')
    remark = forms.CharField(max_length=140,label='remark')
    loginCode = forms.CharField(max_length=100)