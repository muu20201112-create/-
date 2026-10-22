"""Export the editable design/artworks.html to assets/*.png. Run a server on port 8765 first."""
import asyncio
from pathlib import Path
from playwright.async_api import async_playwright

ROOT = Path(__file__).resolve().parents[1]

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(executable_path='/usr/bin/chromium', headless=True, args=['--no-sandbox'])
        page = await browser.new_page(viewport={'width': 1000, 'height': 1000}, device_scale_factor=1)
        await page.goto('http://127.0.0.1:8765/design/artworks.html', wait_until='networkidle')
        await page.evaluate('document.fonts.ready')
        names = await page.locator('.art').evaluate_all('(els) => els.map(e => e.id)')
        for name in names:
            await page.locator('#' + name).screenshot(path=str(ROOT / 'assets' / (name + '.png')))
        await browser.close()
        print(f'Exported {len(names)} images')

asyncio.run(main())
