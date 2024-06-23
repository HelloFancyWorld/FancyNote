from django.urls import path, include
from .views import log_in, log_out, sign_up, get_csrf_token, change_avatar, edit_info, change_password, Call_AI

from rest_framework.routers import DefaultRouter
from .views import UserNoteViewSet, ContentUploadView
router = DefaultRouter()
router.register(r'notes', UserNoteViewSet)


urlpatterns = [
    path('login/', log_in, name='login'),
    path('logout/', log_out, name='logout'),
    path('signup/', sign_up, name='signup'),
    path('get-csrf-token/', get_csrf_token, name='get-csrf-token'),
    path('change_avatar/', change_avatar, name='change_avatar'),
    path('update_user_info/', edit_info, name='edit_info'),
    path('update_password/', change_password, name='edit_password'),
    path('content/upload/', ContentUploadView.as_view(), name='content-upload'),
    path('ai/', Call_AI, name='ai'),
    path('', include(router.urls)),
]
