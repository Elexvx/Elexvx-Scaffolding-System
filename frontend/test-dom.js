import puppeteer from 'puppeteer';

(async () => {
  const browser = await puppeteer.launch();
  const page = await browser.newPage();
  await page.goto('http://localhost:3004/login');
  
  const rightContainer = await page.$('.login-right-container');
  if (rightContainer) {
    const html = await page.evaluate(el => el.outerHTML, rightContainer);
    console.log(html);
  } else {
    console.log('.login-right-container not found');
  }
  
  await browser.close();
})();
