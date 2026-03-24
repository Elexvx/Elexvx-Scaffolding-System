import DOMPurify from 'dompurify';

export function sanitizeHtml(input: string) {
  return DOMPurify.sanitize(String(input || ''), {
    USE_PROFILES: { html: true },
    FORBID_TAGS: ['script', 'style', 'iframe', 'object', 'embed', 'link', 'meta'],
  });
}
