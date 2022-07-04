import socket
import threading
import sys
import pickle
from pyOpenBCI import OpenBCICyton ##
import numpy as np
import time
from time import sleep

class Server():
    def __init__(self, host="192.168.100.101", port=5000):

        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.bind((str(host), int(port)))
        self.sock.listen(10)
        self.sock.setblocking(False)
        self.clients = []
        self.board = None
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
                    'activated': False
                })
                print("Se conecto un cliente: ", addr)
            except:
                pass

    def print_raw(self,sample):
        for c in self.clients:
            try:
                if (c['activated']):
                    msg =(sample.channels_data)
                    msgCodified = str(msg).encode("UTF-8")
                    c['client'].send((len(msgCodified).to_bytes(2, byteorder='big')))
                    c['client'].send((msgCodified))
                    print(type(msg))
                    #time.sleep(0.004)

            except Exception as e: 
                # print(e)
                pass

    def processarCon(self):
        print("ProcessarCon iniciado")
       # print(self.channels_data)
        while True:
            for c in self.clients:
                try:
                    data = c['client'].recv(1024) 
                    if (data.decode("UTF-8") == 'enviar'):
                        c['activated'] = True
                        if not self.board:
                            self.board = OpenBCICyton(daisy = False)
                            self.board.start_stream(self.print_raw)
                            raise OSError('Cannot find OpenBCI port.')
                    if (data.decode("UTF-8") == 'salir'):
                        c['activated'] = False
                except: 
                    pass
Server()