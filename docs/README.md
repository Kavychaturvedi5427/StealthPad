# StealthPad Documentation

The `docs` folder contains the static public pages for StealthPad. It can be opened directly in a browser or published through a static host such as GitHub Pages.

## Files

```text
docs/
├── index.html
├── logo.png
├── stealthpad-faq.html
├── stealthpad-help.html
├── stealthpad-privacy.html
└── stealthpad-terms.html
```

- `index.html`: Main documentation page with links to all sections.
- `stealthpad-faq.html`: Searchable and filterable frequently asked questions.
- `stealthpad-help.html`: Help categories, common questions, and a support form.
- `stealthpad-privacy.html`: Privacy policy with responsive navigation.
- `stealthpad-terms.html`: Terms and conditions with responsive navigation.
- `logo.png`: Shared logo used by the pages.

## Features

The FAQ and Help Center pages include search, category filtering, accordions, responsive navigation, and reduced-motion support. The Help Center support form opens the visitor's email client using a `mailto:` link; it does not submit data to a backend service.

The Privacy Policy and Terms pages include responsive tables of contents, scrollspy navigation, skip links, keyboard focus styles, and back-to-top controls.

All styling and JavaScript are currently embedded directly in the HTML files. No build step or package installation is required.

## Local Preview

Open `docs/index.html` directly, or start a local server from the repository root:

```powershell
python -m http.server 8000 --directory docs
```

Then visit:

```text
http://localhost:8000/
```

## Publishing

Publish the `docs` directory with any static hosting provider. The entry point is:

```text
docs/index.html
```

Before publishing, verify all page links, the shared logo, responsive layouts, search and accordion behavior, support email links, and legal-page navigation.

## Maintenance Notes

- FAQ content is stored in the `FAQS` and `CATEGORIES` arrays in `stealthpad-faq.html`.
- Help content is stored in the `FAQS` and `CATEGORIES` arrays in `stealthpad-help.html`.
- Update the visible last-updated dates when changing legal content.
- Keep privacy-policy statements aligned with the Android app and backend implementation.
- Replace unresolved placeholders such as `YOUR_APP_URL`, `YOUR_PRIVACY_URL`, `YOUR_CANONICAL_URL`, and `YOUR_OG_IMAGE_URL` before production publication.
- Review links containing `#` or the current GitHub Pages URL when changing the deployment location.
- The current support address is `reportthis.kavy.dev@gmail.com`.
