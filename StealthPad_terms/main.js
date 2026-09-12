(function () {
  "use strict";

  var sections = Array.prototype.slice.call(document.querySelectorAll(".legal-content section[id]"));
  var tocLinks = Array.prototype.slice.call(document.querySelectorAll(".toc-list a"));
  var backToTop = document.querySelector(".back-to-top");
  var mobileToc = document.querySelector(".toc-mobile");

  if (!sections.length || !tocLinks.length) {
    // Nothing to spy on — still wire up back-to-top below.
  }

  function setActiveLink(id) {
    tocLinks.forEach(function (link) {
      var isMatch = link.getAttribute("href") === "#" + id;
      link.classList.toggle("is-active", isMatch);
      if (isMatch) {
        link.setAttribute("aria-current", "true");
      } else {
        link.removeAttribute("aria-current");
      }
    });
  }

  // Scrollspy via IntersectionObserver — highlights the current section in the TOC.
  if ("IntersectionObserver" in window && sections.length) {
    var visibleIds = new Set();

    var observer = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          if (entry.isIntersecting) {
            visibleIds.add(entry.target.id);
          } else {
            visibleIds.delete(entry.target.id);
          }
        });

        if (visibleIds.size > 0) {
          // Prefer the section closest to the top of the viewport among visible ones.
          var topMost = sections.find(function (s) {
            return visibleIds.has(s.id);
          });
          if (topMost) {
            setActiveLink(topMost.id);
          }
        }
      },
      {
        rootMargin: "-15% 0px -70% 0px",
        threshold: 0,
      }
    );

    sections.forEach(function (section) {
      observer.observe(section);
    });

    // Set an initial active section on load.
    if (sections[0]) {
      setActiveLink(sections[0].id);
    }
  }

  // Close the mobile TOC after a link is chosen, for a tidy touch experience.
  if (mobileToc) {
    tocLinks.forEach(function (link) {
      if (mobileToc.contains(link)) {
        link.addEventListener("click", function () {
          mobileToc.removeAttribute("open");
        });
      }
    });
  }

  // Back-to-top button — appears after the reader scrolls past the hero.
  if (backToTop) {
    var toggleVisibility = function () {
      if (window.scrollY > 480) {
        backToTop.classList.add("is-visible");
      } else {
        backToTop.classList.remove("is-visible");
      }
    };

    window.addEventListener("scroll", toggleVisibility, { passive: true });
    toggleVisibility();

    backToTop.addEventListener("click", function () {
      var prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
      window.scrollTo({
        top: 0,
        behavior: prefersReducedMotion ? "auto" : "smooth",
      });
      var brandLink = document.querySelector(".brand");
      if (brandLink) {
        brandLink.focus({ preventScroll: true });
      }
    });
  }
})();
