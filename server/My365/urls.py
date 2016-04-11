from django.conf.urls import patterns, include, url
from django.contrib import admin

from My365 import settings
admin.autodiscover()

urlpatterns = patterns('everyday.views',
    url(r'^admin/', include(admin.site.urls)),
    url(r'^$','index'),
    url(r'^image/upload$','image_upload'),
    url(r'^show/(\d+)/$','show_more'),
    url(r'^detail/(\d+)/$','show_image'),
)

if settings.DEBUG:
    urlpatterns += patterns('django.views.static',
        (r'upload/(?P<path>.*)', 'serve', {'document_root': settings.MEDIA_ROOT}),
    )
    
if settings.DEBUG is False:
    urlpatterns += patterns('',
        url(r'^static/(?P<path>.*)$', 'django.views.static.serve', {'document_root': settings.STATIC_ROOT}),
    )