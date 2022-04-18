import socket
import threading
import sys
import pickle
from pyOpenBCI import OpenBCICyton ##

from time import sleep

class Server():
    def __init__(self, host="172.21.104.154", port=5000):

        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.bind((str(host), int(port)))
        self.sock.listen(10)
        self.sock.setblocking(False)

        self.clients = []
        accept = threading.Thread(target=self.aceptarCon)

        process = threading.Thread(target=self.processarCon)

        accept.daemon = True
        accept.start()

        process.daemon = True
        process.start()

        while True:
            msg = input('')
            if msg == 'salir':
                self.sock.close()   
                sys.exit()
            else:
                pass

    def aceptarCon(self):
        print("AceptarCon iniciado")
        while True:
            try:
                conn, addr = self.sock.accept()
                conn.setblocking(False)
                self.clients.append({
                    'client': conn,
                    'data': {}
                })
                print("Se conecto un cliente: ", addr)
            except:
                pass

    def print_raw(self,sample):
        if len(self.clients) > 0:
            for c in self.clients:
                try:
                    data = c['client'].recv(1024) 
                    if (data.decode("UTF-8") == 'enviar'):
                        self.activated = True

                    if (data.decode("UTF-8") == 'salir'):
                        self.activated = False

                    if (self.activated):
                        msg = str(sample.channels_data) 
                        print(msg)
                        msgCodified = msg.encode("UTF-8")
                        c['client'].send((len(msgCodified).to_bytes(2, byteorder='big')))
                        c['client'].send((msgCodified))
                except: 
                    pass

    def processarCon(self):
        print("ProcessarCon iniciado")

        board = OpenBCICyton(daisy = False)
        board.start_stream(self.print_raw)

Server()