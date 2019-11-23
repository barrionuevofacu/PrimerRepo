using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Threading.Tasks;
using WebApiObjetos.Models.Entities;

namespace WebApiObjetos.Domain
{

    [DataContract()]
    public class ImageDTO
    {
        [DataMember()]
        public int Id { get; set; }
        [DataMember()]
        public String Picture { get; set; }
        [DataMember()]
        public int UserId { get; set; }
        [DataMember()]
        public String raza1 { get; set; }
        [DataMember()]
        public String raza2 { get; set; }
        [DataMember()]
        public String raza3 { get; set; }

        public Image ToEntity()
        {
            return new Image
            {
                Id = this.Id,
                Picture = this.Picture,
                UserId = this.UserId,
                raza1 = this.raza1,
                raza2 = this.raza2,
                raza3 = this.raza3

            };
        }
    }
}
