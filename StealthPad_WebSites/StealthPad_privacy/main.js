(function () {
  "use strict";

  var sections = Array.prototype.slice.call(document.querySelectorAll("section[id]"));
  var desktopLinks = Array.prototype.slice.call(document.querySelectorAll('.toc-link[data-toc]'));
  var mobileLinks = Array.prototype.slice.call(document.querySelectorAll('.mobile-toc-list a'));
  var mobileDetails = document.getElementById("mobile-toc-details");
  var backToTop = document.getElementById("back-to-top");

  function setActive(id) {
    desktopLinks.forEach(function (link) {
      link.classList.toggle("is-active", link.getAttribute("href") === "#" + id);
    });
    mobileLinks.forEach(function (link) {
      link.classList.toggle("is-active", link.getAttribute("href") === "#" + id);
    });
  }

  // Scroll-spy via IntersectionObserver
  if ("IntersectionObserver" in window && sections.length) {
    var observer = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          if (entry.isIntersecting) {
            setActive(entry.target.id);
          }
        });
      },
      { rootMargin: "-45% 0px -50% 0px", threshold: 0 }
    );
    sections.forEach(function (section) {
      observer.observe(section);
    });
  }

  // Collapse the mobile "On this page" panel after a link is chosen
  mobileLinks.forEach(function (link) {
    link.addEventListener("click", function () {
      if (mobileDetails) mobileDetails.removeAttribute("open");
    });
  });

  // Back-to-top button visibility + action
  if (backToTop) {
    var toggleBackToTop = function () {
      var show = window.scrollY > 480;
      backToTop.hidden = !show;
    };
    window.addEventListener("scroll", toggleBackToTop, { passive: true });
    toggleBackToTop();

    backToTop.addEventListener("click", function () {
      var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
      window.scrollTo({ top: 0, behavior: reduceMotion ? "auto" : "smooth" });
    });
  }
})();
