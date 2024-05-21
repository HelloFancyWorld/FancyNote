from django.urls import path, include
from .views import log_in, log_out, sign_up, get_csrf_token, change_avatar

urlpatterns = [
    path('login/', log_in, name='login'),
    path('logout/', log_out, name='logout'),
    path('signup/', sign_up, name='signup'),
    path('get-csrf-token/', get_csrf_token, name='get-csrf-token'),
    path('change_avatar/', change_avatar, name='change_avatar'),
]
