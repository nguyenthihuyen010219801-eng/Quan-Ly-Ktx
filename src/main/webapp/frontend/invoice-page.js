(() => {
    const invoices = [
        ["HD0001258", "Nguyễn Văn An", "A101", "Tòa A", "05/2025", "20/05/2025", "30/05/2025", 350000, 120000, 200000, 670000, "Quá hạn"],
        ["HD0001257", "Trần Thị Mai", "B201", "Tòa B", "05/2025", "20/05/2025", "30/05/2025", 320000, 100000, 200000, 620000, "Chưa thanh toán"],
        ["HD0001256", "Lê Minh Đức", "A102", "Tòa A", "05/2025", "19/05/2025", "29/05/2025", 450000, 150000, 250000, 850000, "Quá hạn"],
        ["HD0001255", "Phạm Ngọc Linh", "B202", "Tòa B", "05/2025", "19/05/2025", "29/05/2025", 300000, 100000, 200000, 600000, "Đã thanh toán"],
        ["HD0001254", "Hoàng Gia Bảo", "A103", "Tòa A", "05/2025", "18/05/2025", "28/05/2025", 400000, 120000, 250000, 770000, "Đã thanh toán"],
        ["HD0001253", "Nguyễn Văn An", "A101", "Tòa A", "04/2025", "18/04/2025", "28/04/2025", 380000, 110000, 200000, 690000, "Đã thanh toán"],
        ["HD0001252", "Trần Thị Mai", "B201", "Tòa B", "04/2025", "17/04/2025", "27/04/2025", 250000, 90000, 150000, 490000, "Đã thanh toán"],
        ["HD0001251", "Lê Minh Đức", "A102", "Tòa A", "04/2025", "17/04/2025", "27/04/2025", 260000, 80000, 150000, 490000, "Chưa thanh toán"],
        ["HD0001250", "Phạm Ngọc Linh", "B202", "Tòa B", "04/2025", "16/04/2025", "26/04/2025", 210000, 70000, 100000, 380000, "Quá hạn"],
        ["HD0001249", "Hoàng Gia Bảo", "A103", "Tòa A", "04/2025", "16/04/2025", "26/04/2025", 200000, 70000, 100000, 370000, "Đã thanh toán"]
    ];
    const money = (value) => Number(value).toLocaleString("vi-VN") + " đ";
    const statusClass = (status) => status === "Đã thanh toán" ? "paid" : status === "Đang xử lý" ? "processing" : status === "Đã hủy" ? "cancelled" : status === "Quá hạn" ? "overdue" : "unpaid";

    function injectInvoiceUi() {
        const main = document.querySelector("main.main");
        if (!main || document.querySelector("#invoicesView")) return;
        main.insertAdjacentHTML("beforeend", `
          <section id="invoicesView" class="view invoice-view">
            <div class="invoice-stats">
              ${statCard("blue", "fa-regular fa-file-lines", "Tổng số hóa đơn", "1.248", "up", "12,5%", "so với tháng trước")}
              ${statCard("green", "fa-solid fa-sack-dollar", "Hóa đơn đã thanh toán", "1.098", "up", "10,8%", "so với tháng trước")}
              ${statCard("orange", "fa-solid fa-receipt", "Hóa đơn chưa thanh toán", "150", "down", "8,3%", "so với tháng trước")}
              ${statCard("red", "fa-solid fa-money-bill-wave", "Tổng doanh thu", "3.568.250.000 đ", "up", "15,2%", "so với tháng trước")}
            </div>

            <form class="invoice-filter" id="invoiceFilterForm">
              ${inputField("Từ ngày", "invoiceFrom", "date", "2025-05-01")}
              ${inputField("Đến ngày", "invoiceTo", "date", "2025-05-23")}
              ${selectField("Trạng thái", "invoiceStatus", ["Tất cả", "Đã thanh toán", "Chưa thanh toán", "Quá hạn"])}
              ${selectField("Tòa nhà", "invoiceBuilding", ["Tất cả", "Tòa A", "Tòa B"])}
              ${selectField("Phòng", "invoiceRoom", ["Tất cả", "A101", "A102", "A103", "B201", "B202"])}
              <label class="invoice-search-control"><span>Tìm kiếm</span><div><input id="invoiceKeyword" placeholder="Nhập mã hóa đơn, tên SV, phòng..."><i class="fa-solid fa-magnifying-glass"></i></div></label>
              <div class="invoice-filter-actions">
                <button class="invoice-btn search" type="submit"><i class="fa-solid fa-magnifying-glass"></i>Tìm kiếm</button>
                <button class="invoice-btn reset" id="invoiceReset" type="button"><i class="fa-solid fa-rotate"></i>Làm mới</button>
                <button class="invoice-btn create" id="invoiceCreate" type="button"><i class="fa-solid fa-plus"></i>Tạo hóa đơn</button>
                <button class="invoice-btn export" id="invoiceExport" type="button"><i class="fa-regular fa-file-excel"></i>Xuất Excel</button>
              </div>
            </form>

            <div class="invoice-dashboard">
              <article class="invoice-card revenue-card">
                <div class="invoice-card-head"><h3>Biểu đồ doanh thu theo tháng</h3><select><option>6 tháng gần nhất</option></select></div>
                <div class="bar-chart"><div class="y-labels"><span>800M</span><span>600M</span><span>400M</span><span>200M</span><span>0</span></div><div class="bars">${[[42,"T12"],[60,"T1"],[68,"T2"],[46,"T3"],[48,"T4"],[66,"T5"]].map(([h,l])=>`<div><i style="height:${h}%"></i><span>${l}</span></div>`).join("")}</div></div>
              </article>
              <article class="invoice-card status-card"><h3>Tỷ lệ trạng thái hóa đơn</h3><div class="donut-wrap"><div class="invoice-donut"><div><small>Tổng cộng</small><strong>1.248</strong></div></div><ul><li><i class="paid"></i><span>Đã thanh toán<small>88.0% (1.098)</small></span></li><li><i class="unpaid"></i><span>Chưa thanh toán<small>12.0% (150)</small></span></li><li><i class="overdue"></i><span>Quá hạn<small>3.2% (40)</small></span></li></ul></div></article>
              <article class="invoice-card building-revenue"><h3>Doanh thu theo tòa nhà 05/2025</h3>${buildingBar("Tòa nhà A",100,"1.450.000.000 đ")}${buildingBar("Tòa nhà B",76,"1.120.000.000 đ")}${buildingBar("Tòa nhà C",44,"620.000.000 đ")}${buildingBar("Tòa nhà D",18,"250.000.000 đ")}${buildingBar("Tòa nhà E",10,"128.250.000 đ")}</article>
              <article class="invoice-card invoice-summary"><h3>Tổng quan hóa đơn</h3>${summaryLine("fa-solid fa-arrow-up","Tổng hóa đơn","1.248")}${summaryLine("fa-regular fa-circle-check","Đã thanh toán","1.098","green")}${summaryLine("fa-regular fa-clock","Chưa thanh toán","150","orange")}${summaryLine("fa-regular fa-circle-xmark","Quá hạn","40","red")}${summaryLine("fa-solid fa-percent","Tỷ lệ thanh toán","88,0%")} ${summaryLine("fa-solid fa-coins","Doanh thu","3.568.250.000 đ")}<button>Xem chi tiết hóa đơn</button></article>
            </div>

            <div class="invoice-bottom">
              <article class="invoice-card invoice-list"><h3>Danh sách hóa đơn</h3><div class="invoice-table-wrap"><table><thead><tr><th>STT</th><th>Mã hóa đơn</th><th>Sinh viên</th><th>Phòng</th><th>Tòa nhà</th><th>Kỳ thanh toán</th><th>Ngày tạo</th><th>Ngày đến hạn</th><th>Tiền điện</th><th>Tiền nước</th><th>Dịch vụ</th><th>Tổng tiền</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody id="invoiceTableBody"></tbody></table></div><div class="invoice-pagination"><span>Hiển thị <b>1</b> đến <b>10</b> trong tổng số <b>1.248</b> hóa đơn</span><div><select><option>10 / trang</option></select><button disabled><i class="fa-solid fa-chevron-left"></i></button><button class="active">1</button><button>2</button><button>3</button><button>4</button><button>5</button><em>...</em><button>125</button><button><i class="fa-solid fa-chevron-right"></i></button></div></div></article>
              <article class="invoice-card overdue-list"><h3>Hóa đơn quá hạn</h3>${overdue("HD0001258 - Nguyễn Văn An - A101","-2.450.000 đ","Quá hạn 5 ngày")}${overdue("HD0001256 - Lê Minh Đức - A102","-3.200.000 đ","Quá hạn 3 ngày")}${overdue("HD0001250 - Phạm Ngọc Linh - B202","-2.700.000 đ","Quá hạn 2 ngày")}${overdue("HD0001247 - Trần Thị Mai - B201","-1.800.000 đ","Quá hạn 1 ngày")}<button>Xem tất cả hóa đơn quá hạn</button></article>
            </div>
          </section>`);
        document.head.insertAdjacentHTML("beforeend", `<style>${invoiceCss()}</style>`);
        document.head.insertAdjacentHTML("beforeend", `<style>${invoiceTableCss()}</style>`);
        renderInvoices(invoices);
        setupInvoiceEvents();
    }

    function statCard(color, icon, label, value, trend, percent, note) { return `<article class="invoice-stat"><div class="stat-icon ${color}"><i class="${icon}"></i></div><div><span>${label}</span><strong>${value}</strong><small class="${trend}"><i class="fa-solid fa-arrow-${trend === "up" ? "up" : "down"}"></i> ${percent} <em>${note}</em></small></div></article>`; }
    function inputField(label,id,type,value){return `<label><span>${label}</span><input id="${id}" type="${type}" value="${value}"></label>`;}
    function selectField(label,id,items){return `<label><span>${label}</span><select id="${id}">${items.map(x=>`<option>${x}</option>`).join("")}</select></label>`;}
    function buildingBar(label,width,value){return `<div class="building-row"><span>${label}</span><i><b style="width:${width}%"></b></i><strong>${value}</strong></div>`;}
    function summaryLine(icon,label,value,color=""){return `<div class="summary-line ${color}"><i class="${icon}"></i><span>${label}</span><strong>${value}</strong></div>`;}
    function overdue(code,value,note){return `<div class="overdue-item"><div><strong>${code}</strong><small>${note}</small></div><b>${value}</b></div>`;}

    function renderInvoices(items) {
        const body = document.querySelector("#invoiceTableBody"); if (!body) return;
        body.innerHTML = items.length ? items.map((x,index)=>`<tr><td>${index+1}</td><td><a>${x[0]}</a></td><td><strong>${x[1]}</strong></td><td>${x[2]}</td><td>${x[3]}</td><td>${x[4]}</td><td>${x[5]}</td><td>${x[6]}</td><td>${money(x[7])}</td><td>${money(x[8])}</td><td>${money(x[9])}</td><td><strong>${money(x[10])}</strong></td><td><span class="invoice-status ${statusClass(x[11])}">${x[11]}</span></td><td><button class="invoice-eye" title="Xem chi tiết"><i class="fa-regular fa-eye"></i></button></td></tr>`).join("") : `<tr><td colspan="14" class="invoice-empty">Không tìm thấy hóa đơn phù hợp</td></tr>`;
    }

    function setupInvoiceEvents(){
        document.querySelector("#invoiceFilterForm").addEventListener("submit",event=>{event.preventDefault();filterInvoices();});
        document.querySelector("#invoiceReset").addEventListener("click",()=>{document.querySelector("#invoiceFilterForm").reset();renderInvoices(invoices);});
        document.querySelector("#invoiceCreate").addEventListener("click",()=>alert("Chức năng tạo hóa đơn đã sẵn sàng để kết nối dữ liệu."));
        document.querySelector("#invoiceExport").addEventListener("click",()=>alert("Đã chuẩn bị xuất danh sách hóa đơn."));
    }
    function filterInvoices(){const keyword=document.querySelector("#invoiceKeyword").value.trim().toLowerCase(),status=document.querySelector("#invoiceStatus").value,building=document.querySelector("#invoiceBuilding").value,room=document.querySelector("#invoiceRoom").value;renderInvoices(invoices.filter(x=>(!keyword||[x[0],x[1],x[2]].some(v=>v.toLowerCase().includes(keyword)))&&(status==="Tất cả"||x[11]===status)&&(building==="Tất cả"||x[3]===building)&&(room==="Tất cả"||x[2]===room)));}

    function invoiceTableCss(){return `
      .invoice-list{min-width:0;overflow:hidden;border:1px solid #e5e7eb;border-radius:15px;background:#fff;box-shadow:0 10px 28px rgba(23,43,77,.08)}
      .invoice-list>h3{padding:17px 18px 14px;font-size:14px;font-weight:800;color:#1f3f68;border-bottom:1px solid #e5e7eb}
      .invoice-table-wrap{max-height:520px;overflow:auto;margin:0;scrollbar-width:thin;scrollbar-color:#9db6d1 #eef3f8}
      .invoice-table-wrap::-webkit-scrollbar{width:7px;height:7px}
      .invoice-table-wrap::-webkit-scrollbar-track{background:#eef3f8;border-radius:999px}
      .invoice-table-wrap::-webkit-scrollbar-thumb{background:#9db6d1;border:1px solid #eef3f8;border-radius:999px}
      .invoice-table-wrap::-webkit-scrollbar-thumb:hover{background:#6e94bc}
      .invoice-list table{width:100%;min-width:1510px;border-collapse:separate;border-spacing:0;table-layout:auto;font-size:12px}
      .invoice-list th{position:sticky;top:0;z-index:3;height:50px;padding:0 15px;vertical-align:middle;background:#f5f9ff;color:#24466f;font-size:11px;font-weight:800;letter-spacing:.35px;text-transform:uppercase;border-right:1px solid #e5e7eb;border-bottom:1px solid #dce5f0;white-space:nowrap;box-shadow:0 1px 0 rgba(36,70,111,.04)}
      .invoice-list td{height:60px;padding:8px 15px;vertical-align:middle;color:#344563;border-right:1px solid #e5e7eb;border-bottom:1px solid #e5e7eb;white-space:nowrap}
      .invoice-list th:last-child,.invoice-list td:last-child{border-right:0}
      .invoice-list tbody tr{transition:background-color .2s ease}
      .invoice-list tbody tr:hover{background:#f8fbff}
      .invoice-list tbody tr:last-child td{border-bottom:0}
      .invoice-list th:nth-child(1),.invoice-list td:nth-child(1){min-width:55px;text-align:center}
      .invoice-list th:nth-child(2),.invoice-list td:nth-child(2){min-width:115px}
      .invoice-list th:nth-child(3),.invoice-list td:nth-child(3){min-width:155px}
      .invoice-list th:nth-child(4),.invoice-list td:nth-child(4){min-width:75px;text-align:center}
      .invoice-list th:nth-child(5),.invoice-list td:nth-child(5){min-width:105px}
      .invoice-list th:nth-child(6),.invoice-list td:nth-child(6){min-width:110px;text-align:center}
      .invoice-list th:nth-child(7),.invoice-list td:nth-child(7){min-width:145px;font-variant-numeric:tabular-nums}
      .invoice-list th:nth-child(8),.invoice-list td:nth-child(8){min-width:125px;font-variant-numeric:tabular-nums}
      .invoice-list th:nth-child(9),.invoice-list td:nth-child(9),.invoice-list th:nth-child(10),.invoice-list td:nth-child(10),.invoice-list th:nth-child(11),.invoice-list td:nth-child(11){min-width:105px;text-align:right;font-variant-numeric:tabular-nums}
      .invoice-list th:nth-child(12),.invoice-list td:nth-child(12){min-width:125px;text-align:right}
      .invoice-list th:nth-child(13),.invoice-list td:nth-child(13){min-width:130px;text-align:center}
      .invoice-list th:nth-child(14),.invoice-list td:nth-child(14){min-width:80px;text-align:center}
      .invoice-list td:nth-child(2) a{color:#0869e8;font-weight:800;text-decoration:none}
      .invoice-list td:nth-child(3) strong{color:#172b4d;font-weight:700}
      .invoice-list td:nth-child(12)>strong{color:#174a7e;font-weight:600;font-variant-numeric:tabular-nums}
      .invoice-status{display:inline-flex;min-width:108px;min-height:29px;align-items:center;justify-content:center;padding:6px 11px;border:1px solid transparent;border-radius:999px;font-size:10px;font-weight:800;line-height:1}
      .invoice-status.paid{color:#087f46;background:#e8f8ef;border-color:#bde8cf}
      .invoice-status.unpaid,.invoice-status.overdue{color:#d9363e;background:#ffebed;border-color:#ffc9cd}
      .invoice-status.processing{color:#a86000;background:#fff4d6;border-color:#ffe0a3}
      .invoice-status.cancelled{color:#64748b;background:#edf0f4;border-color:#d9dee6}
      .invoice-list td:last-child button,.invoice-eye{width:40px;height:40px;display:inline-grid;place-items:center;padding:0;border:1px solid #dce5f0;border-radius:8px;background:#fff;color:#0869e8;box-shadow:0 3px 8px rgba(23,43,77,.1);transition:transform .18s ease,background-color .18s ease,box-shadow .18s ease,color .18s ease}
      .invoice-list td:last-child button:hover,.invoice-eye:hover{transform:translateY(-2px) scale(1.04);background:#edf5ff;color:#045fc8;box-shadow:0 7px 14px rgba(23,43,77,.16)}
      .invoice-list td:last-child button:focus-visible,.invoice-eye:focus-visible{outline:3px solid rgba(22,119,242,.2);outline-offset:2px}
      .invoice-pagination{display:flex;justify-content:space-between;align-items:center;gap:18px;min-height:62px;padding:13px 18px;border-top:1px solid #e5e7eb;background:#fff;color:#64748b;font-size:11px}
      .invoice-pagination>div{display:flex;align-items:center;justify-content:flex-end;gap:7px;flex-wrap:wrap}
      .invoice-pagination select{height:36px;padding:0 10px;border:1px solid #d8e0ec;border-radius:7px;background:#fff;color:#344563}
      .invoice-pagination button{width:36px;height:36px;border:1px solid #d8e0ec;border-radius:7px;background:#fff;color:#344563;transition:.18s}
      .invoice-pagination button:not(:disabled):hover{transform:translateY(-1px);border-color:#8eb7e6;background:#f5f9ff;color:#0869e8}
      .invoice-pagination button.active{background:#0869e8;color:#fff;border-color:#0869e8;box-shadow:0 5px 12px rgba(8,105,232,.22)}
      .invoice-pagination button:disabled{opacity:.4;cursor:not-allowed}
      @media(max-width:1250px){.invoice-list table{min-width:1510px}.invoice-table-wrap{max-height:480px}}
      @media(max-width:760px){.invoice-list{border-radius:12px}.invoice-list>h3{padding:14px}.invoice-list table{min-width:1510px}.invoice-list td{height:57px}.invoice-pagination{align-items:flex-start;flex-direction:column}.invoice-pagination>div{justify-content:flex-start}.invoice-table-wrap{max-height:430px}}
    `;}

    function invoiceCss(){return `
      .invoice-view.active{display:block;overflow:auto;padding-bottom:16px;color:#16233b}.invoice-breadcrumb{display:flex;align-items:center;gap:14px;margin:0 0 16px;color:#617089;font-size:13px}.invoice-breadcrumb i{font-size:9px;color:#9aa7b8}.invoice-breadcrumb strong{font-weight:600}.invoice-stats{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;margin-bottom:12px}.invoice-stat{min-height:106px;display:flex;align-items:center;gap:16px;padding:18px;background:#fff;border:1px solid #e3e9f2;border-radius:8px;box-shadow:0 3px 12px rgba(17,44,86,.04)}.stat-icon{width:52px;height:52px;flex:0 0 52px;display:grid;place-items:center;border-radius:50%;color:#fff;font-size:21px}.stat-icon.blue{background:linear-gradient(135deg,#1677f2,#0756d9)}.stat-icon.green{background:linear-gradient(135deg,#16bc68,#009a4d)}.stat-icon.orange{background:linear-gradient(135deg,#ffad32,#ff870f)}.stat-icon.red{background:linear-gradient(135deg,#ff6565,#f23842)}.invoice-stat>div:last-child{display:grid;gap:4px}.invoice-stat span{font-size:12px;font-weight:700}.invoice-stat strong{font-size:22px;line-height:1.1}.invoice-stat small{font-size:11px;font-weight:700}.invoice-stat small.up{color:#08a657}.invoice-stat small.down{color:#f07923}.invoice-stat small em{color:#718096;font-style:normal;font-weight:500}.invoice-filter{display:grid;grid-template-columns:repeat(5,minmax(120px,1fr)) minmax(230px,1.6fr);gap:12px;padding:15px;margin-bottom:12px;background:#fff;border:1px solid #e3e9f2;border-radius:8px;box-shadow:0 3px 12px rgba(17,44,86,.04)}.invoice-filter label{display:grid;gap:7px}.invoice-filter label>span{font-size:11px;font-weight:700}.invoice-filter input,.invoice-filter select{height:38px;width:100%;padding:0 11px;border:1px solid #d8e0ec;border-radius:5px;background:#fff;color:#263752;outline:none}.invoice-filter input:focus,.invoice-filter select:focus{border-color:#1677f2;box-shadow:0 0 0 3px rgba(22,119,242,.1)}.invoice-search-control div{position:relative}.invoice-search-control input{padding-right:36px}.invoice-search-control div i{position:absolute;right:12px;top:12px;color:#38547a}.invoice-filter-actions{grid-column:1/-1;display:flex;justify-content:flex-end;gap:12px}.invoice-btn{height:38px;padding:0 18px;display:flex;align-items:center;gap:8px;border-radius:5px;border:1px solid #d4deeb;background:#fff;color:#1868cb;font-weight:700}.invoice-btn.search{background:#0869e8;color:#fff;border-color:#0869e8}.invoice-btn.create{background:#08a653;color:#fff;border-color:#08a653}.invoice-btn.export{color:#128c4f}.invoice-dashboard{display:grid;grid-template-columns:1.5fr 1.12fr 1fr .86fr;gap:12px;margin-bottom:12px}.invoice-card{background:#fff;border:1px solid #e3e9f2;border-radius:8px;box-shadow:0 3px 12px rgba(17,44,86,.04)}.invoice-card h3{margin:0;padding:15px 15px 7px;font-size:12px}.invoice-card-head{display:flex;justify-content:space-between;align-items:center}.invoice-card-head select{height:29px;margin:10px;border:1px solid #d8e0ec;border-radius:4px;font-size:10px}.bar-chart{height:180px;display:flex;padding:8px 18px 15px}.y-labels{width:40px;display:flex;flex-direction:column;justify-content:space-between;padding-bottom:24px;color:#53657e;font-size:9px}.bars{flex:1;display:flex;align-items:flex-end;justify-content:space-around;border-bottom:1px solid #dfe6f0;background:repeating-linear-gradient(to bottom,#fff 0,#fff 34px,#e9eef5 35px)}.bars>div{height:100%;width:35px;display:flex;flex-direction:column;justify-content:flex-end;align-items:center;gap:8px;font-size:10px}.bars i{width:19px;display:block;background:linear-gradient(#1479eb,#0865dc);border-radius:2px 2px 0 0}.donut-wrap{height:185px;display:flex;align-items:center;justify-content:center;gap:25px}.invoice-donut{width:128px;height:128px;border-radius:50%;display:grid;place-items:center;background:conic-gradient(#08ad59 0 88%,#ff9d21 88% 96.8%,#ff565d 96.8%);position:relative}.invoice-donut:before{content:"";position:absolute;inset:22px;background:#fff;border-radius:50%}.invoice-donut div{position:relative;z-index:1;display:grid;text-align:center}.invoice-donut small{font-size:10px}.invoice-donut strong{font-size:17px}.donut-wrap ul{list-style:none;margin:0;padding:0;display:grid;gap:14px}.donut-wrap li{display:flex;gap:8px;font-size:10px}.donut-wrap li>i{width:12px;height:12px;border-radius:50%}.donut-wrap i.paid{background:#08ad59}.donut-wrap i.unpaid{background:#ff9d21}.donut-wrap i.overdue{background:#ff565d}.donut-wrap li span{display:grid;gap:3px}.donut-wrap li small{color:#66778f}.building-revenue{padding-bottom:12px}.building-row{display:grid;grid-template-columns:58px 1fr 83px;gap:7px;align-items:center;padding:7px 13px;font-size:9px}.building-row i{height:12px;background:#edf2f8}.building-row b{display:block;height:100%;background:#096ce3;border-radius:1px}.building-row strong{text-align:right;font-size:9px}.invoice-summary{grid-row:span 1}.summary-line{display:grid;grid-template-columns:16px 1fr auto;gap:7px;padding:7px 14px;align-items:center;font-size:9.5px}.summary-line>i{color:#24466f}.summary-line.green>i{color:#08a657}.summary-line.orange>i{color:#ff9718}.summary-line.red>i{color:#ef3d46}.summary-line strong{font-size:10px}.invoice-summary button,.overdue-list>button{display:block;margin:9px auto 13px;border:0;background:transparent;color:#0871e8;font-size:10px}.invoice-bottom{display:grid;grid-template-columns:minmax(0,1fr) 245px;gap:12px}.invoice-list{min-width:0}.invoice-table-wrap{overflow:auto;margin:4px 10px}.invoice-list table{width:100%;min-width:1150px;border-collapse:collapse;font-size:9px}.invoice-list th{height:34px;padding:0 9px;text-align:left;background:#f5f7fa;white-space:nowrap}.invoice-list td{height:34px;padding:0 9px;border-bottom:1px solid #edf0f4;white-space:nowrap}.invoice-list td a{color:#0871e8}.invoice-list td:nth-child(1),.invoice-list td:last-child{text-align:center}.invoice-status{padding:4px 7px;border-radius:5px;font-size:8px;font-weight:700}.invoice-status.paid{color:#05944a;background:#e9faef}.invoice-status.unpaid{color:#e57a00;background:#fff4dc}.invoice-status.overdue{color:#ef3d46;background:#ffeded}.invoice-eye{border:0;background:transparent;color:#0871e8}.invoice-empty{text-align:center;height:80px!important;color:#718096}.invoice-pagination{display:flex;justify-content:space-between;align-items:center;padding:9px 14px 14px;color:#718096;font-size:9.5px}.invoice-pagination>div{display:flex;align-items:center;gap:5px}.invoice-pagination select{height:32px;border:1px solid #d8e0ec;border-radius:4px}.invoice-pagination button{width:31px;height:31px;border:1px solid #d8e0ec;border-radius:4px;background:#fff}.invoice-pagination button.active{background:#0869e8;color:#fff;border-color:#0869e8}.overdue-list{align-self:start}.overdue-item{display:flex;justify-content:space-between;gap:8px;padding:12px 14px;border-bottom:1px solid #edf0f4;font-size:9px}.overdue-item div{display:grid;gap:5px}.overdue-item small{color:#718096}.overdue-item>b{color:#ed323b;white-space:nowrap}.invoice-view::-webkit-scrollbar,.invoice-table-wrap::-webkit-scrollbar{width:6px;height:6px}.invoice-view::-webkit-scrollbar-thumb,.invoice-table-wrap::-webkit-scrollbar-thumb{background:#a9bdd4;border-radius:999px}@media(max-width:1250px){.invoice-stats{grid-template-columns:repeat(2,1fr)}.invoice-filter{grid-template-columns:repeat(3,1fr)}.invoice-dashboard{grid-template-columns:repeat(2,1fr)}.invoice-bottom{grid-template-columns:1fr}.overdue-list{display:grid;grid-template-columns:repeat(2,1fr)}.overdue-list h3,.overdue-list>button{grid-column:1/-1}}@media(max-width:760px){.invoice-stats,.invoice-dashboard{grid-template-columns:1fr}.invoice-filter{grid-template-columns:1fr}.invoice-filter-actions{flex-wrap:wrap;justify-content:stretch}.invoice-btn{flex:1;justify-content:center}.invoice-bottom{display:block}.overdue-list{display:block;margin-top:12px}.invoice-pagination{align-items:flex-start;gap:10px;flex-direction:column}.invoice-stat{min-height:92px}.donut-wrap{gap:14px}}
    `;}

    injectInvoiceUi();
    window.renderInvoiceView = () => {
        document.querySelectorAll("#buildingsView,#roomsView,#servicesView,#managementDetailView").forEach(view=>view.classList.remove("active"));
        document.querySelector("#invoicesView")?.classList.add("active");
    };
})();
