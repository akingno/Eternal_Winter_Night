const pages=[["index.html","首页"],["getting-started.html","快速上手"],["world.html","极地世界"],["survival.html","生存与温度"],["items.html","物品与设备"],["civilization.html","村庄与交易"],["vanilla-changes.html","原版改动"]];
const current=document.body.dataset.page||"index.html";
document.querySelectorAll("[data-nav]").forEach(nav=>{nav.innerHTML=pages.map(([href,label])=>`<a href="${href}" class="${href===current?"active":""}">${label}</a>`).join("")});
document.querySelectorAll("[data-menu]").forEach(button=>button.addEventListener("click",()=>{const nav=document.querySelector(".nav-links");const open=nav.classList.toggle("open");button.setAttribute("aria-expanded",String(open))}));
