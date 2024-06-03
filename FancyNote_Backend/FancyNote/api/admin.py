from django.contrib import admin
from .models import User_info, User_note, Content, User_note, TextContent, ImageContent, AudioContent


class TextContentInline(admin.StackedInline):
    model = TextContent
    extra = 1


class ImageContentInline(admin.StackedInline):
    model = ImageContent
    extra = 1


class AudioContentInline(admin.StackedInline):
    model = AudioContent
    extra = 1


class ContentInline(admin.StackedInline):
    model = Content
    extra = 0

    def content_display(self, obj):
        if obj.text_content:
            return obj.text_content.text
        elif obj.image_content:
            return obj.image_content.local_path
        elif obj.audio_content:
            return obj.audio_content.local_path
        return None

    readonly_fields = ['content_display']
    fields = ['id', 'type', 'content_display']
    verbose_name_plural = 'Contents'


@admin.register(User_note)
class UserNoteAdmin(admin.ModelAdmin):
    inlines = [ContentInline]
    list_display = ['id', 'user', 'title', 'created_at', 'updated_at']


admin.site.register(User_info)

admin.site.register(ImageContent)
