from PIL import Image
import io
import os

import requests
from bottle import Bottle, static_file, redirect, response, request

base_dir = './static'
app = Bottle()


@app.hook('after_request')
def enable_cors():
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Methods'] = 'PUT, GET, POST, DELETE, OPTIONS'
    response.headers['Access-Control-Allow-Headers'] = 'Origin, Accept, Content-Type, X-Requested-With, X-CSRF-Token'


@app.route('/', method='OPTIONS')
@app.route('/<path:path>', method='OPTIONS')
def options_handler():
    return


@app.route('/', method='GET')
def index():
    return redirect("/apps/clue/")


@app.route('/<path:path>', method='GET')
def serve_file(path: str):
    if request.query_string:
        path += "?" + request.query_string
    file_path = os.path.join(base_dir, path)
    if path.endswith('/'):
        file_path += "dir.html"
    _download_file_if_not_exists(path, file_path)
    return _serve_content(file_path)


def _serve_content(file_path: str):
    if file_path.endswith(".webp"):
        return _serve_webp_as_png(file_path)
    if file_path.endswith(".js"):
        return _update_urls_and_serve(file_path)
    return static_file(file_path, root=".")


def _update_urls_and_serve(file_path: str):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    modified_content = content.replace("https://runeapps.org", "")
    response.content_type = 'text/javascript'
    return modified_content


def _serve_webp_as_png(webp_path: str):
    png_path = webp_path.replace(".webp", ".png")
    if not os.path.exists(png_path):
        with Image.open(webp_path) as img:
            img.save(png_path, 'PNG')
    return _serve_content(png_path)


def _download_file_if_not_exists(path: str, file_path: str):
    if not os.path.exists(file_path):
        print("Fetching", path)
        resp = requests.get(f'https://runeapps.org/{path}')
        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        with open(file_path, 'wb') as f:
            f.write(resp.content)


if __name__ == '__main__':
    app.run(port=8811)
