import sae
from My365 import wsgi

application = sae.create_wsgi_app(wsgi.application)
