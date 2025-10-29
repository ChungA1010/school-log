#include <iostream>
#include <fstream>
#include <sstream>
#include <iomanip>
#include <map>
#include <string>
#include <regex>
#include <vector>
#include <algorithm>
using namespace std;

map<string, string> OPTAB;
map<string, int> SYMTAB;
map<string, int> LITTAB;
map<string, pair<int,int> > SECTION_INFO;
map<string, map<string, int> > SECTION_SYMTAB;

int LOCCTR = 0;
int STARTADDR = 0;
int LASTADDR = 0;

// ────────────────────────────────
// 유틸 함수
// ────────────────────────────────
string trim(const string &s) {
    string res = s;
    res.erase(remove(res.begin(), res.end(), '\t'), res.end());
    while (res.find("  ") != string::npos)
        res.replace(res.find("  "), 2, " ");
    if (!res.empty() && res[0] == ' ') res.erase(0, 1);
    if (!res.empty() && res.back() == ' ') res.pop_back();
    return res;
}

int getOperandSize(const string &operand) {
    if (operand.empty()) return 0;
    if (operand[0] == 'C') return operand.size() - 3;
    if (operand[0] == 'X') return (operand.size() - 3) / 2;
    return 0;
}

void loadOPTAB() {
    ifstream fin("optab.txt");
    string opcode, hex;
    while (fin >> opcode >> hex) {
        for (auto &c : opcode) c = toupper(c);
        OPTAB[opcode] = hex;
    }
    fin.close();
}

// ────────────────────────────────
// PASS 1
// ────────────────────────────────
void pass1() {
    ifstream fin("SRCFILE.txt");
    ofstream fout("intfile.txt");
    if (!fin.is_open()) {
        cerr << "input.txt 파일을 열 수 없습니다.\n";
        return;
    }

    loadOPTAB();
    string line;
    int lineNo = 0;

    SYMTAB.clear();
    LITTAB.clear();

    fout << left
         << setw(6) << "LINE"
         << setw(8) << "LOC"
         << setw(12) << "LABEL"
         << setw(12) << "OPCODE"
         << setw(20) << "OPERAND" << "\n";
    fout << string(60, '-') << "\n";

    // 첫 줄 (START)
    getline(fin, line);
    line = trim(line);
    vector<string> tokens;
    string word;
    stringstream ss1(line);
    while (ss1 >> word) tokens.push_back(word);

    string label = "-", opcode = "", operand = "";
    if (tokens.size() == 3) {
        label = tokens[0]; opcode = tokens[1]; operand = tokens[2];
    } else if (tokens.size() == 2) {
        label = tokens[0]; opcode = tokens[1]; operand = "";
    } else if (tokens.size() == 1) {
        label = "-"; opcode = tokens[0]; operand = "";
    }

    for (size_t i = 0; i < opcode.size(); i++) opcode[i] = toupper(opcode[i]);

    // 첫 줄의 label이 섹션 이름으로 설정
    string curSection = (label != "-") ? label : "NONAME";

    if (!operand.empty() && opcode == "START")
        STARTADDR = stoi(operand, nullptr, 16);
    else
        STARTADDR = 0;
    LOCCTR = STARTADDR;

    SECTION_INFO[curSection].first = LOCCTR;

    {
        ostringstream linebuf;
        linebuf << right << setw(4) << setfill('0') << lineNo++
                << "  " << hex << uppercase << setw(6) << setfill('0') << LOCCTR
                << "  " << setfill(' ')
                << left << setw(12) << label
                << setw(12) << opcode
                << setw(20) << operand;
        fout << linebuf.str() << "\n";
    }

    // 본문
    while (getline(fin, line)) {
        line = trim(line);
        if (line.empty() || line[0] == '.') continue;

        vector<string> parts;
        string tok;
        stringstream ss(line);
        while (ss >> tok) parts.push_back(tok);

        label = "-"; opcode = ""; operand = "";
        if (parts.size() == 3) {
            label = parts[0]; opcode = parts[1]; operand = parts[2];
        } else if (parts.size() == 2) {
            label = "-"; opcode = parts[0]; operand = parts[1];
        } else if (parts.size() == 1) {
            label = "-"; opcode = parts[0]; operand = "";
        }

        for (size_t i = 0; i < opcode.size(); i++) opcode[i] = toupper(opcode[i]);

        {
            ostringstream linebuf;
            linebuf << right << setw(4) << setfill('0') << lineNo++
                    << "  " << hex << uppercase << setw(6) << setfill('0') << LOCCTR
                    << "  " << setfill(' ')
                    << left << setw(12) << label
                    << setw(12) << opcode
                    << setw(20) << operand;
            fout << linebuf.str() << "\n";
        }

        // CSECT 새 섹션 시작
        if (opcode == "CSECT") {
    SECTION_INFO[curSection].second = LOCCTR - SECTION_INFO[curSection].first;
    curSection = (label != "-") ? label : "NONAME";
    SECTION_INFO[curSection].first = LOCCTR; // 기존 LOCCTR로 시작
    continue;
}


        // SYMBOL 등록
        if (label != "-") {
            if (SECTION_SYMTAB[curSection].count(label))
                cerr << "[에러] 중복된 심볼: " << label << " (" << curSection << ")" << endl;
            else
                SECTION_SYMTAB[curSection][label] = LOCCTR;
        }

        if (OPTAB.count(opcode)) LOCCTR += 3;
        else if (opcode == "WORD") LOCCTR += 3;
        else if (opcode == "RESW") LOCCTR += 3 * stoi(operand);
        else if (opcode == "RESB") LOCCTR += stoi(operand);
        else if (opcode == "BYTE") LOCCTR += getOperandSize(operand);
        else if (opcode == "EQU" || opcode == "BASE" || opcode == "LTORG" || opcode == "NOBASE") {
        } else if (opcode == "END") {
            SECTION_INFO[curSection].second = LOCCTR - SECTION_INFO[curSection].first;
            LASTADDR = LOCCTR;
            continue;
        }
    }

    fin.close();
    fout.close();

    // symtab.txt 출력
    ofstream symout("symtab.txt");
    if (!symout.is_open()) {
        cerr << "symtab.txt 파일을 생성할 수 없습니다.\n";
        return;
    }

    // 섹션별 주소 누적 계산
    int runningAddr = STARTADDR;
    for (auto &sec : SECTION_INFO) {
        sec.second.first = runningAddr;
        runningAddr += sec.second.second;
    }

    symout << left
        << setw(12) << "SECTION"
        << setw(14) << "SYMBOL"
        << setw(14) << "ADDRESS"
        << setw(10) << "LENGTH" << "\n";
    symout << string(50, '-') << "\n";

    for (auto &sec : SECTION_INFO) {
    // 시작 주소 & 길이 → 6자리 16진수로 포맷
    ostringstream ssStart, ssLen;
    ssStart << "0x" << setw(6) << setfill('0') << uppercase << hex << sec.second.first;
    ssLen   << setw(6) << setfill('0') << uppercase << hex << sec.second.second;

    // SECTION 행
    symout << left
           << setw(12) << sec.first
           << setw(14) << ssStart.str()
           << setw(14) << " "
           << ssLen.str() << "\n";

    // SYMBOL 행
    if (SECTION_SYMTAB.count(sec.first) && !SECTION_SYMTAB[sec.first].empty()) {
        for (auto &sym : SECTION_SYMTAB[sec.first]) {
            ostringstream ssAddr;
            ssAddr << "0x" << setw(6) << setfill('0') << uppercase << hex << sym.second;

            symout << setw(12) << " "
                   << setw(14) << sym.first
                   << setw(14) << ssAddr.str()
                   << "\n";
        }
    }
}


    for (auto &sec : SECTION_SYMTAB) {
        for (auto &sym : sec.second) {
            SYMTAB[sym.first] = sym.second;
        }
    }
    symout.close();
    cout << "[PASS1 완료] symtab.txt (섹션별 주소 누적 포함) 생성됨\n";
}

// ────────────────────────────────
// TEXT RECORD 출력 함수
// ────────────────────────────────
void flushText(ofstream &fout, int &textStart, string &textData) {
    if (!textData.empty()) {
        stringstream len;
        len << setw(2) << setfill('0') << hex << uppercase << (int)(textData.size() / 2);
        fout << "T" << setw(6) << setfill('0') << right << uppercase << hex << textStart
             << len.str() << textData << "\n";
        textData.clear();
    }
}

// ────────────────────────────────
// OBJ CODE 생성
// ────────────────────────────────
string makeObjCode(string opcode, string operand) {
    string upperOpcode = opcode;
    for (size_t i = 0; i < upperOpcode.size(); i++) upperOpcode[i] = toupper(upperOpcode[i]);

    if (!OPTAB.count(upperOpcode)) return "";

    string code = OPTAB[upperOpcode];
    if (operand.empty()) return code + "0000";

    if (SYMTAB.count(operand)) {
        stringstream ss;
        ss << setw(4) << setfill('0') << hex << uppercase << SYMTAB[operand];
        code += ss.str();
    } else {
        code += "0000";
    }
    return code;
}

// ────────────────────────────────
// PASS 2
// ────────────────────────────────
void pass2() {
    ifstream fin("intfile.txt");
    int runningAddr = STARTADDR;
    for (auto &sec : SECTION_INFO) {
        sec.second.first = runningAddr;
        runningAddr += sec.second.second;
    }

    ofstream fout("objfile.txt");
    if (!fin.is_open()) {
        cerr << "intfile.txt 파일 열기 실패\n";
        return;
    }

    string line, loc, label, opcode, operand;
    string progName;
    int startAddr = 0, firstExec = 0;
    const int MAX_TEXT_LEN = 60;

    vector<string> modRecords;
    bool sectionOpen = false;
    int textStart = 0;
    string textData = "";

    int runningAddr = STARTADDR; // 첫 섹션은 START에서 시작

    while (getline(fin, line)) {
        if (line.find("-----") != string::npos || line.find("LINE") != string::npos)
            continue;

        stringstream ss(line);
        int lineNo;
        ss >> lineNo >> loc >> label >> opcode >> operand;
        if (opcode.empty()) continue;
        for (size_t i = 0; i < opcode.size(); i++) opcode[i] = toupper(opcode[i]);

        if (opcode == "START" || opcode == "CSECT") {  
            if (sectionOpen) {
                flushText(fout, textStart, textData);
                for (size_t i = 0; i < modRecords.size(); i++)
                    fout << modRecords[i] << "\n";
                modRecords.clear();
                fout << "E" << setw(6) << setfill('0')
                    << uppercase << hex << firstExec << "\n";
            }
            sectionOpen = true;

            progName = label;
            string pname = progName.empty() ? "NONAME" : progName.substr(0, 6);

            // startAddr = runningAddr;
            // firstExec = startAddr;

            // startAddr = (!operand.empty() && operand != "CSECT")
            //             ? stoi(operand, nullptr, 16)
            //             : 0;
            // firstExec = startAddr;

            // 🔸 길이 계산: PASS1에서 SECTION_INFO[progName].second 가져오기
            int length = 0;
            if (SECTION_INFO.count(progName))
                length = SECTION_INFO[progName].second;
            else
                length = LASTADDR - stoi(loc, nullptr, 16); // fallback

            // 🔸 다음 섹션 시작 주소를 위해 누적 증가
            runningAddr += length;

            // 🔸 길이 출력 (6자리 HEX)
            ostringstream lenbuf;
            lenbuf << setw(6) << setfill('0') << uppercase << hex << length;

            fout << "H"
                 << left << setw(6) << setfill(' ') << pname
                 << right << setw(6) << setfill('0') << uppercase << hex << startAddr
                 << lenbuf.str() << "\n";

            continue;
        }

        if (opcode == "END") {
        flushText(fout, textStart, textData);
        for (auto &m : modRecords) fout << m << "\n";
        modRecords.clear();

        // ✅ E 레코드는 해당 섹션의 시작 주소를 가리킴
        fout << "E" << setw(6) << setfill('0')
            << uppercase << hex << firstExec << "\n";

        sectionOpen = false;
        continue;
    }

        if (opcode == "RESW" || opcode == "RESB") {
            flushText(fout, textStart, textData);
            continue;
        }

        if (OPTAB.count(opcode)) {
            if (loc.empty()) continue;
            string obj = makeObjCode(opcode, operand);
            int locval = stoi(loc, nullptr, 16);

            if (textData.empty()) textStart = locval;
            if ((textData.size() + obj.size()) > MAX_TEXT_LEN)
                flushText(fout, textStart, textData);

            textData += obj;

            if (!opcode.empty() && opcode[0] == '+') {
                stringstream mrec;
                mrec << "M" << setw(6) << setfill('0') << right << uppercase << hex
                     << (locval + 1)
                     << setw(2) << 5
                     << "+" << "MAIN";
                modRecords.push_back(mrec.str());
            }
        }
    }

    if (sectionOpen) {
    flushText(fout, textStart, textData);
    for (auto &m : modRecords) fout << m << "\n";

    // ✅ 섹션별 종료 주소를 자기 시작 주소로
    fout << "E" << setw(6) << setfill('0')
         << uppercase << hex << firstExec << "\n";
    }   

    fin.close();
    fout.close();
    cout << "[PASS2 완료] objfile.txt 생성됨 (CSECT, M레코드 완성)\n";
}

int main() {
    pass1();
    pass2();
    return 0;
}
