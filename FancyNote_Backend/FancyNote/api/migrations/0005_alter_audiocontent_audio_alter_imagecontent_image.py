# Generated by Django 5.0.6 on 2024-06-02 15:22

import api.models
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0004_audiocontent_local_path_imagecontent_local_path'),
    ]

    operations = [
        migrations.AlterField(
            model_name='audiocontent',
            name='audio',
            field=models.FileField(upload_to=api.models.audio_path),
        ),
        migrations.AlterField(
            model_name='imagecontent',
            name='image',
            field=models.ImageField(upload_to=api.models.image_path),
        ),
    ]