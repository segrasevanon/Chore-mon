const fs = require('fs');
const path = require('path');
const { instance } = require('@viz-js/viz');
const sharp = require('sharp');
const root = __dirname;
const dir = path.join(root, 'diagrams');
const schema = JSON.parse(fs.readFileSync(path.join(root, 'schema.json'), 'utf8'));
const esc = s => s.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');

function er(full) {
  const dot = [
    'digraph ER {',
    'graph [rankdir=LR, bgcolor="white", pad=0.35, nodesep=0.55, ranksep=0.85, splines=ortho, fontname="Arial", label="Choreemon backend physical schema", labelloc=t, fontsize=22];',
    'node [shape=plain, fontname="Arial", fontsize=12];',
    'edge [fontname="Arial", fontsize=10, color="#60758B", arrowsize=0.8, arrowhead=odotcrow, arrowtail=teetee, dir=both];'
  ];
  for (const t of schema) {
    const fields = t.columns.filter(c => full || t.pk.includes(c.name) || t.fk.some(k => k.column === c.name));
    let label = `<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="7" COLOR="#CBD5E1"><TR><TD COLSPAN="2" BGCOLOR="#183B56"><FONT COLOR="white"><B>${t.name}</B></FONT></TD></TR>`;
    fields.forEach((c, i) => {
      const key = [t.pk.includes(c.name) ? 'PK' : '', t.fk.some(k => k.column === c.name) ? 'FK' : ''].filter(Boolean).join(' ');
      const note = full ? `  ${c.type}${c.nullable ? ' NULL' : ''}` : '';
      label += `<TR><TD ALIGN="LEFT" BGCOLOR="${i % 2 ? '#F1F5F9' : 'white'}">${esc(key)}</TD><TD PORT="${c.name}" ALIGN="LEFT" BGCOLOR="${i % 2 ? '#F1F5F9' : 'white'}">${esc(c.name + note)}</TD></TR>`;
    });
    label += '</TABLE>';
    dot.push(`${t.name} [label=<${label}>];`);
  }
  for (const t of schema) for (const fk of t.fk) {
    dot.push(`${fk.parent} -> ${t.name};`);
  }
  dot.push('}');
  return dot.join('\n');
}

const architecture = `digraph Architecture {
graph [rankdir=TB, newrank=true, bgcolor=white, pad=0.25, nodesep=0.4, ranksep=0.5, fontname="Arial", fontsize=20, label="Current independent execution paths", labelloc=t];
node [shape=box, style="rounded,filled", fillcolor="#F1F5F9", color="#CBD5E1", fontname="Arial", fontsize=13, margin="0.15,0.12"];
edge [color="#60758B", fontname="Arial", fontsize=10];
subgraph cluster_android { label="Offline Android app"; fontname="Arial"; fontsize=16; color="#B7D6E8";
appUser [label="Shared device users", fillcolor="#DBEAFE"];
compose [label="Compose screens\\nand MainActivity"];
vm [label="GameViewModel\\nand StateFlow"];
rules [label="Local GameRules"];
prefs [label="Private JSON preferences", shape=cylinder];
media [label="System photo picker and local URIs"];
appUser -> compose -> vm;
vm -> rules [dir=both]; vm -> prefs [label="SaveCodec"]; compose -> media;
}
subgraph cluster_backend { label="Independent Ktor backend"; fontname="Arial"; fontsize=16; color="#C4C8E9";
apiClient [label="Arbitrary or future\\nAPI clients", fillcolor="#EDE9FE"];
ktor [label="Ktor routes and JWT guards"];
repo [label="Repositories and\\nbackend GameRules"];
sql [label="SQL Server and Flyway", shape=cylinder];
files [label="Local uploads", shape=cylinder];
fcm [label="Legacy FCM sender"];
stubs [label="Azure identity\\nand Blob stubs", fillcolor="#FEF3C7"];
apiClient -> ktor -> repo -> sql;
ktor -> files; ktor -> fcm; ktor -> stubs [style=dashed,label="ENV azure"];
}
}`;

const trust = `digraph Trust {
graph [rankdir=TB, bgcolor=white, pad=0.3, nodesep=0.45, ranksep=0.45, fontname="Arial", fontsize=20, label="Trust boundaries and sensitive data flows", labelloc=t];
node [shape=box, style="rounded,filled", fillcolor="#F1F5F9", color="#CBD5E1", fontname="Arial", fontsize=12, margin="0.16,0.1"];
edge [fontname="Arial", fontsize=10, color="#60758B"];
subgraph cluster_device {label="TB1 Shared device and parent UI"; color="#93C5FD"; fontname="Arial";
localUser [label="Child or adult on device"];
ui [label="Android UI and PIN gate"];
localUser -> ui;
subgraph cluster_store {label="TB2 App storage and media"; color="#BFDBFE"; fontname="Arial";
save [label="Private game JSON and photo URIs",shape=cylinder];}
ui -> save;
}
client [label="External or authenticated API actor",fillcolor="#EDE9FE"];
subgraph cluster_api {label="TB3 Network to Ktor"; color="#C4B5FD"; fontname="Arial";
api [label="JWT checks and route policy"];
cmd [label="Repositories and game commands"];
api -> cmd;
}
client -> api [label="Credentials, JWT, IDs, file bytes"];
subgraph cluster_persist {label="TB4 Persistence"; color="#94A3B8"; fontname="Arial";
db [label="SQL Server",shape=cylinder]; files [label="Uploads",shape=cylinder];}
cmd -> db [label="Household and reward data"]; api -> files [label="Photo write or read"];
subgraph cluster_cloud {label="TB5 Azure adapters"; color="#FBBF24"; fontname="Arial";
azure [label="Unsigned verifier and URL-only storage stubs",fillcolor="#FEF3C7"];}
api -> azure [style=dashed,label="Selected in Azure mode"];
subgraph cluster_egress {label="TB6 Messaging and operations"; color="#FCA5A5"; fontname="Arial";
push [label="FCM tokens and notification data"];
logs [label="Request and notification logs"];}
api -> push; api -> logs;
}`;

function canvas(w, h, title) {
  return [`<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}"><defs><marker id="arrow" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto"><path d="M0 0 L10 5 L0 10 Z" fill="#60758B"/></marker><marker id="one" markerWidth="12" markerHeight="16" refX="10" refY="8" orient="auto-start-reverse"><path d="M2 1 V15 M7 1 V15" fill="none" stroke="#60758B" stroke-width="2"/></marker><marker id="many" markerWidth="25" markerHeight="20" refX="24" refY="10" orient="auto"><circle cx="6" cy="10" r="4" fill="white" stroke="#60758B" stroke-width="1.5"/><path d="M12 10 L24 2 M12 10 L24 10 M12 10 L24 18" fill="none" stroke="#60758B" stroke-width="1.5"/></marker></defs><rect width="100%" height="100%" fill="white"/><text x="30" y="37" font-family="Arial" font-size="27" font-weight="bold" fill="#183B56">${esc(title)}</text>`];
}
function textBlock(out, x, y, lines, size=22, color='#183B56', anchor='middle') {
  out.push(`<text x="${x}" y="${y}" text-anchor="${anchor}" font-family="Arial" font-size="${size}" fill="${color}">${lines.map((line,i)=>`<tspan x="${x}" dy="${i ? size*1.2 : 0}">${esc(line)}</tspan>`).join('')}</text>`);
}
function card(out,x,y,w,h,lines,color='#F1F5F9') {
  out.push(`<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="10" fill="${color}" stroke="#CBD5E1" stroke-width="2"/>`);
  textBlock(out,x+w/2,y+h/2-((lines.length-1)*13)+8,lines);
}
function edge(out, d, er=false, dashed=false) {
  out.push(`<path d="${d}" fill="none" stroke="#60758B" stroke-width="2" ${dashed?'stroke-dasharray="7 5"':''} marker-end="url(#${er?'many':'arrow'})" ${er?'marker-start="url(#one)"':''}/>`);
}
function panel(out,x,y,w,h,title) {
  out.push(`<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="8" fill="#FBFDFF" stroke="#B7CADA" stroke-width="2"/>`);
  textBlock(out,x+18,y+31,[title],23,'#183B56','start');
}
function compactArchitecture() {
  const out=canvas(1200,930,'Current independent execution paths');
  panel(out,25,65,540,825,'Offline Android app');
  panel(out,590,65,585,825,'Independent Ktor backend');
  for(const d of ['M295 205 V250','M295 335 V380','M295 465 V510','M100 425 H40 V695 H55','M490 290 H548 V695 H530']) edge(out,d);
  card(out,130,130,330,75,['Shared device users'],'#DBEAFE');
  card(out,100,250,390,85,['Compose screens','and MainActivity']);
  card(out,100,380,390,85,['GameViewModel','and StateFlow']);
  card(out,100,510,390,85,['Local GameRules','and save orchestration']);
  card(out,55,645,240,100,['Private JSON','preferences']);
  card(out,315,645,215,100,['System picker','photo URIs']);
  textBlock(out,295,810,['No implemented API client','No network permission'],22);
  for(const d of ['M880 205 V250','M880 335 V380','M760 465 V510','M1085 295 H1155 V550 H1140','M675 295 H610 V695 H625']) edge(out,d);
  edge(out,'M1085 315 H1160 V695 H1140',false,true);
  card(out,710,130,340,75,['API clients'],'#EDE9FE');
  card(out,675,250,410,85,['Ktor routes and JWT guards']);
  card(out,675,380,410,85,['Repositories and','backend GameRules']);
  card(out,625,510,270,80,['SQL Server','and Flyway']);
  card(out,925,510,215,80,['Local uploads']);
  card(out,625,645,270,100,['FCM sender','Legacy protocol']);
  card(out,925,645,215,100,['Azure adapter','placeholders'],'#FEF3C7');
  textBlock(out,880,810,['Adapters selected by ServiceRegistry','Azure mode is incomplete'],21);
  out.push('</svg>'); return out.join('');
}
function compactTrust() {
  const out=canvas(1200,950,'Trust boundaries and sensitive data flows');
  panel(out,25,70,420,790,'Shared device path');
  card(out,70,145,330,90,['TB1 Child or adult','Android UI and PIN gate'],'#DBEAFE');
  edge(out,'M235 235 V340');
  card(out,70,340,330,110,['TB2 Private game JSON','and selected media URIs']);
  textBlock(out,235,550,['Device sandbox is trusted','Local PIN gates management','Local profiles share data'],22);
  panel(out,475,70,700,790,'Independent network API path');
  card(out,645,140,360,80,['External API actor'],'#EDE9FE');
  edge(out,'M825 220 V280');
  card(out,615,280,420,95,['TB3 Ktor JWT validation','and route authorization']);
  edge(out,'M720 375 V455'); edge(out,'M925 375 V455',false,true);
  card(out,505,455,320,110,['TB4 SQL and local files','Tenant data and rewards']);
  card(out,850,455,290,110,['TB5 Azure adapters','Unsigned auth stub','URL-only storage stub'],'#FEF3C7');
  edge(out,'M825 375 V410 H1160 V700 H1100');
  card(out,550,660,550,110,['TB6 Messaging and logs','Device tokens and family activity']);
  textBlock(out,825,815,['Credentials and requests cross TB3','Identity and tenant checks must precede access'],21);
  textBlock(out,30,915,['The current Android app does not call the API. Boundary numbers match the threat register.'],20,'#60758B','start');
  out.push('</svg>');return out.join('');
}
function compactER() {
  const out=canvas(1200,1000,'Choreemon backend entity relationships');
  textBlock(out,30,73,['PK primary key    FK foreign key    Double bar one    Circle and crow foot zero or many'],19,'#60758B','start');
  for(const d of ['M285 200 H360','M155 242 V380','M655 210 H835','M655 250 H730 V420 H835','M510 284 V380','M285 460 H360','M980 284 V380','M155 519 V650 H980 V603','M450 284 V315 H325 V790 H360','M600 284 V300 H780 V790 H835']) edge(out,d,true);
  function table(x,y,w,name,fields) {
    const h=55+fields.length*42;
    out.push(`<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="white" stroke="#CBD5E1" stroke-width="2"/><rect x="${x}" y="${y}" width="${w}" height="55" fill="#183B56"/>`);
    textBlock(out,x+w/2,y+35,[name],22,'white');
    fields.forEach((f,i)=>{if(i%2)out.push(`<rect x="${x+1}" y="${y+55+i*42}" width="${w-2}" height="42" fill="#F1F5F9"/>`);textBlock(out,x+14,y+84+i*42,[f],21,'#183B56','start');});
  }
  table(30,145,255,'Households',['PK  id']);
  table(360,145,295,'Members',['PK  id','FK  household_id']);
  table(835,145,325,'Creatures',['PK  id','FK  owner_id']);
  table(30,380,255,'Chores',['PK  id','FK  household_id']);
  table(360,380,295,'ChoreAssignments',['PK FK  chore_id','PK FK  member_id']);
  table(835,380,325,'Completions',['PK  id','FK  chore_id','FK  member_id','FK  creature_id']);
  table(360,735,295,'DeviceTokens',['PK  id','FK  member_id']);
  table(835,735,325,'ClaimedMilestones',['PK FK  member_id','PK  milestone_key']);
  textBlock(out,30,950,['8 tables and 10 foreign keys from Flyway V1 to V7. All foreign keys are required.'],20,'#60758B','start');
  out.push('</svg>');return out.join('');
}

(async () => {
  const viz = await instance();
  for (const [name, dot] of [['er-full', er(true)], ['er-overview', er(false)], ['architecture', architecture], ['trust-boundaries', trust]]) {
    fs.writeFileSync(path.join(dir, name + '.dot'), dot);
    const svg = viz.renderString(dot, {format:'svg', engine:'dot'});
    fs.writeFileSync(path.join(dir, name + '.svg'), svg);
    await sharp(Buffer.from(svg), {density:160}).png().toFile(path.join(dir, name + '.png'));
    console.log(name + ' rendered');
  }
  for (const [name, svg] of [['er-overview',compactER()],['architecture',compactArchitecture()],['trust-boundaries',compactTrust()]]) {
    fs.writeFileSync(path.join(dir,name+'.svg'),svg);
    await sharp(Buffer.from(svg),{density:180}).png().toFile(path.join(dir,name+'.png'));
    console.log(name+' compact layout rendered');
  }
})().catch(e => { console.error(e); process.exit(1); });
